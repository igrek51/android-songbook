import java.util.Properties
import java.io.FileInputStream
import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

fun getVersionCode(): Int {
    val stdout = ByteArrayOutputStream()
    exec {
        this.commandLine = listOf("git", "tag", "--list")
        this.standardOutput = stdout
    }
    return stdout.toString().split("\n").count { it.isNotBlank() } + 1800
}

fun getVersionName(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        this.commandLine = listOf("git", "describe", "--tags", "--dirty", "--always")
        this.standardOutput = stdout
    }
    return stdout.toString().trim()
}

@Suppress("UnstableApiUsage")
android {
    namespace = "igrek.songbook"
    compileSdk = 34
    defaultConfig {
        applicationId = "igrek.songbook"
        minSdk = 21 // Android 5.0 Lollipop
        targetSdk = 34 // Android 14
        versionCode = getVersionCode()
        versionName = getVersionName()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }
    signingConfigs {
        create("release") {
            val propsFile = rootProject.file(".keystore.properties")
            if (propsFile.exists()) {
                val props = Properties()
                props.load(FileInputStream(propsFile))
                storeFile = file(props["storeFile"] as String)
                storePassword = props["storePassword"] as String
                keyAlias = props["keyAlias"] as String
                keyPassword = props["keyPassword"] as String
            }
        }
    }
    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("java.util.Date", "BUILD_DATE", "new java.util.Date(" + System.currentTimeMillis() + "L)")
        }
        register("prerelease") {
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("java.util.Date", "BUILD_DATE", "new java.util.Date(" + System.currentTimeMillis() + "L)")
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("java.util.Date", "BUILD_DATE", "new java.util.Date(" + System.currentTimeMillis() + "L)")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xallow-result-return-type",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi",
        )
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4" // based on https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    }
    configurations.all {
        resolutionStrategy {
            force("com.google.code.findbugs:jsr305:1.3.9")
        }
    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // Android
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1") // Settings layouts
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.work:work-runtime:2.8.1")
    // Kotlin
    val kotlinVersion = rootProject.extra.get("kotlin_version") as String
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    // Material Design Components
    implementation("com.google.android.material:material:1.10.0")
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.material3:material3:1.1.2") // Material Design 3
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.8.1") // Integration with activities
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    // Google APIs
    implementation("com.google.api-client:google-api-client-android:1.35.2") {
        exclude(group="org.apache.httpcomponents")
    }
    implementation("com.google.android.gms:play-services-basement:18.2.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    // AdMob
    implementation("com.google.android.gms:play-services-ads:22.5.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.3") {
        exclude(module="httpclient")
        exclude(module="commons-logging")
    }
    implementation("com.google.apis:google-api-services-drive:v3-rev197-1.25.0") {
        exclude(group="org.apache.httpcomponents")
    }
    // Google Play Billing
    implementation("com.android.billingclient:billing-ktx:5.2.1")
    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.0")
    // Guava
    implementation("com.google.guava:guava:32.1.3-android")
    // Apache Commons Codec
    implementation("commons-codec:commons-codec:1.16.0")
    // Protobuf
    implementation("com.google.protobuf:protobuf-kotlin:3.25.1")
    // RX
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    // JWT
    implementation("com.auth0.android:jwtdecode:2.0.2")
    // Socket.IO
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group="org.json", module="json")
    }
    // iText PDF
    implementation("com.itextpdf.android:kernel-android:7.2.5")
    // Unit tests
    testImplementation("androidx.appcompat:appcompat:1.6.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    // Android instumentation tests
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("org.mockito:mockito-core:5.7.0")
    androidTestImplementation("org.mockito:mockito-android:5.7.0")
    androidTestImplementation("org.assertj:assertj-core:3.24.2")
}

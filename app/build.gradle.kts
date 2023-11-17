import java.util.Properties
import java.io.FileInputStream
import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization") //version "1.9.0"
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

fun getVersionCode(): Int {
    val stdout = ByteArrayOutputStream()
    exec {
        this.commandLine = listOf("git", "tag", "--list")
        this.standardOutput = stdout
    }
    return stdout.toString().split("\n").size + 1800
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
    compileSdk = 33
    defaultConfig {
        applicationId = "igrek.songbook"
        minSdk = 21 // Android 5.0 Lollipop
        targetSdk = 33 // Android 13
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("java.util.Date", "BUILD_DATE", "new java.util.Date(" + System.currentTimeMillis() + "L)")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
        kotlinCompilerExtensionVersion = "1.4.8" // based on https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    }
    configurations.all {
        resolutionStrategy {
            force("com.google.code.findbugs:jsr305:1.3.9")
        }
    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // Android
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.legacy:legacy-preference-v14:1.0.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.work:work-runtime:2.8.1")
    // Kotlin
    val kotlinVersion = rootProject.extra.get("kotlin_version") as String
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    // Material Design Components
    implementation("com.google.android.material:material:1.9.0")
    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.06.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3:1.1.2") // Material Design 3
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.7.2") // Integration with activities
    // Firebase
    implementation("com.google.firebase:firebase-analytics-ktx:21.3.0")
    implementation("com.google.firebase:firebase-crashlytics:18.4.2")
    // Google APIs
    implementation("com.google.api-client:google-api-client-android:1.35.2") {
        exclude(group="org.apache.httpcomponents")
    }
    implementation("com.google.android.gms:play-services-basement:18.2.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    // AdMob
    implementation("com.google.android.gms:play-services-ads:22.4.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.1") {
        exclude(module="httpclient")
        exclude(module="commons-logging")
    }
    implementation("com.google.apis:google-api-services-drive:v3-rev197-1.25.0") {
        exclude(group="org.apache.httpcomponents")
    }
    // Google Play Billing
    implementation("com.android.billingclient:billing-ktx:5.2.1")
    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    // Guava
    implementation("com.google.guava:guava:31.1-android")
    // Apache Commons Codec
    implementation("commons-codec:commons-codec:1.15")
    // Protobuf
    implementation("com.google.protobuf:protobuf-kotlin:3.22.2")
    // RX
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:3.12.12") // last version supporting API 1
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.12")
    // JWT
    implementation("com.auth0.android:jwtdecode:2.0.2")
    // Socket.IO
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group="org.json", module="json")
    }
    // PDFBox
    implementation("com.tom-roush:pdfbox-android:2.0.25.0") // don't upgrade due to huge bundle size
    // Unit tests
    testImplementation("androidx.appcompat:appcompat:1.6.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:2.28.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    // Android instumentation tests
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("org.mockito:mockito-core:2.28.2")
    androidTestImplementation("org.mockito:mockito-android:2.22.0")
    androidTestImplementation("org.assertj:assertj-core:3.24.2")
}

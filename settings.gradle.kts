rootProject.buildFileName = "build.gradle.kts"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}

include(":app")

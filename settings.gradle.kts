pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "1.9.20"
        kotlin("android") version "1.9.20"
        kotlin("kapt") version "1.9.20"
        id("com.android.application") version "8.5.1"
        id("androidx.navigation.safeargs") version "2.7.7"
        id("de.undercouch.download") version "5.0.1"
    }
}

rootProject.name = "CameraProject"
include(":app")
//include(":ktorServer")


//rootProject.name = "DriveTestCam"
//include(":app")
//include(":ktorServer")

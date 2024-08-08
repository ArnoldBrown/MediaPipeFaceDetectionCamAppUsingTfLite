// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    // Top-level variables used for versioning
    val kotlinVersion by extra("1.9.20")
    val javaVersion by extra(JavaVersion.VERSION_1_8)

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
        classpath("de.undercouch:gradle-download-task:5.0.1")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
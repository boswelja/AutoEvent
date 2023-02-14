pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "7.4.1"
        id("com.android.library") version "7.4.1"
        id("org.jetbrains.kotlin.android") version "1.8.10"
        id("com.squareup.wire") version "4.5.1"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "AutoEvent"
include(":app")

enableFeaturePreview("VERSION_CATALOGS")

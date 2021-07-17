pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "7.1.0-alpha03"
        id("com.android.library") version "7.1.0-alpha03"
        id("org.jetbrains.kotlin.android") version "1.5.21"
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

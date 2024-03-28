plugins {
    id("com.android.application")
    kotlin("android")
    id("com.squareup.wire")
}

android {
    namespace = "com.boswelja.autoevent"

    compileSdk = 34

    defaultConfig {
        applicationId = "com.boswelja.autoevent"
        minSdk = 26
        targetSdk = 34
        versionCode = PackageInfo.getVersionCode()
        versionName = PackageInfo.getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures.compose = true
    buildFeatures.buildConfig = true
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.work)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.datastore.proto)

    implementation(libs.kotlinx.coroutines.play)
    implementation(libs.bundles.compose)
    implementation(libs.mlkit.entityextraction)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
    testImplementation(libs.strikt.mockk)
    testImplementation(libs.mockk.core)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.strikt.core)
    androidTestImplementation(libs.strikt.mockk)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.mockk.android)
}

wire {
    kotlin {}
}

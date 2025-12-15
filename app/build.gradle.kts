plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
//    alias(libs.plugins.google.gms)
}

android {
    namespace = "sdf.bitt.hydromate"
    compileSdk = 36

    defaultConfig {
        applicationId = "sdf.bitt.hydromate"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Haze
    implementation(libs.haze.jetpack.compose)
    implementation(libs.androidx.compose.foundation)
    // Date/Time API for older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    // Firebase
//    implementation(platform(libs.firebase.bom))
//    implementation(libs.firebase.analytics)
//    implementation(libs.firebase.messaging)
//    implementation(libs.firebase.database)
    // Splash
    implementation(libs.androidx.core.splashscreen)
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.webkit)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.core.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.firebase.database.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.appcompat)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    // Extended Icon Library
    implementation(libs.androidx.material.icons.extended)
    // Material
    implementation(libs.androidx.material)
    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)
    // LiveData
    implementation(libs.androidx.runtime.livedata)
    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.viewbinding)
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
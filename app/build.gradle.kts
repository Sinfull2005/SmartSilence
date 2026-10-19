
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.smartsilence"

    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.smartsilence"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // Keep TensorFlow Lite assets uncompressed.
    // TensorFlowLiteInferenceEngine uses AssetManager.openFd().
    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    // =========================================================
    // JETPACK COMPOSE
    // =========================================================

    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.runtime:runtime")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // =========================================================
    // ANDROID CORE
    // =========================================================

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // =========================================================
    // SECURITY / BIOMETRIC AUTHENTICATION
    // =========================================================

    implementation("androidx.biometric:biometric:1.1.0")

    // =========================================================
    // LOCATION SERVICES
    // =========================================================

    implementation("com.google.android.gms:play-services-location:21.4.0")

    // =========================================================
    // ROOM DATABASE
    // =========================================================

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // =========================================================
    // ON-DEVICE MACHINE LEARNING
    // =========================================================

    implementation("com.google.ai.edge.litert:litert:1.4.0")

    // =========================================================
    // UNIT TESTING
    // =========================================================

    testImplementation(libs.junit)

    // =========================================================
    // INSTRUMENTED TESTING
    // Use the same Compose BOM to supply the UI test version.
    // =========================================================

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // =========================================================
    // DEBUG
    // =========================================================

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

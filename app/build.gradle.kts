plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.signify"
    compileSdk = 35

    aaptOptions {
        noCompress ("tflite", "task")
    }

    defaultConfig {
        applicationId = "com.example.signify"
        minSdk = 25
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.coil.compose)
    implementation(libs.lottie.compose)

    //For Camera
    val cameraxVersion = "1.3.0-rc01"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")

    implementation("androidx.camera:camera-extensions:$cameraxVersion")
    implementation("com.google.guava:guava:31.0.1-android")

    //MediaPipe imports
    implementation("com.google.mediapipe:tasks-vision:0.10.7") // Use the latest version
    implementation("org.tensorflow:tensorflow-lite:2.13.0") // Or your TFLite version

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("nl.dionsegijn:konfetti-compose:2.0.2")





//    MediaPipe imports
//
//    val latest_version="0.10.21"
//    implementation("com.google.mediapipe:tasks-vision:$latest_version")
//    implementation("com.google.mediapipe:solution-core:$latest_version")
//    implementation("com.google.mediapipe:tasks-components:$latest_version")

//    implementation("org.tensorflow:tensorflow-lite:$latest_version")
//    implementation("org.tensorflow:tensorflow-lite-support:$latest_version")
//    implementation("org.tensorflow:tensorflow-lite-metadata:$latest_version")

//    // If you want to use the base sdk
//    implementation 'com.google.mlkit:pose-detection:18.0.0-beta5'
//    // If you want to use the accurate sdk
//    implementation 'com.google.mlkit:pose-detection-accurate:18.0.0-beta5'
}
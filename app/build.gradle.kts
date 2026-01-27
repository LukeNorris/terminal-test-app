plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.terminal_test_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.terminal_test_app"
        minSdk = 24
        targetSdk = 35
        versionCode = 11
        versionName = "2.6.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(
                providers.gradleProperty("TERMINAL_STORE_FILE").get()
            )
            storePassword = providers.gradleProperty("TERMINAL_STORE_PASSWORD").get()
            keyAlias = providers.gradleProperty("TERMINAL_KEY_ALIAS").get()
            keyPassword = providers.gradleProperty("TERMINAL_KEY_PASSWORD").get()
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {

    // --------------------
    // Compose
    // --------------------
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.compose.material:material-icons-extended")


    // --------------------
    // Hilt (DI)
    // --------------------
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")

    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")



    // --------------------
    // Networking
    // --------------------
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    // --------------------
    // Kotlin Serialization
    // --------------------
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // --------------------
    // Camera / Scanner
    // --------------------
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")
    implementation("androidx.camera:camera-mlkit-vision:1.4.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")

    // --------------------
    // Storage
    // --------------------
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // --------------------
    // Required for Hilt (fixes your crash)
    // --------------------
    implementation("com.squareup:javapoet:1.13.0")

    // --------------------
    // Test
    // --------------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

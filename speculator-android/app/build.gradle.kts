group = "speculator.app.android"
version = "1.0.0"
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.speculator"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.speculator"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.recyclerview)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("speculator.engine:lib:1.0.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation(platform("ai.djl:bom:0.33.0"))
    implementation("ai.djl.android:core")
    runtimeOnly("ai.djl.android:pytorch-native")
    runtimeOnly("ai.djl.android:onnxruntime")
    // Source: https://mvnrepository.com/artifact/com.microsoft.onnxruntime/onnxruntime
}


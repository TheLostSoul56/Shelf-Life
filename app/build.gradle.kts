import org.gradle.kotlin.dsl.support.kotlinCompilerOptions

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.shelflife"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.shelflife"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.gson)
    implementation(libs.activity)
    testImplementation(libs.junit)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    //implementation for qrCode
    implementation(libs.zxing.android.embedded)

    //also for cameraX
    implementation(libs.concurrent.futures.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    //implementation for cameraX
    //def camerax_version

    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.view)
    implementation(libs.camera.lifecycle)



    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
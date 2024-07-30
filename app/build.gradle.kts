plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")

}

android {
    namespace = "com.example.myapplicationwmsystem"
    compileSdk = 34

    defaultConfig {

        applicationId = "com.example.myapplicationwmsystem"
        minSdk = 24
        targetSdk = 34
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
val freeDebugImplementation by configurations.creating

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation( "androidx.compose.ui:ui:1.6.8")
    freeDebugImplementation("com.mapbox.navigation:core:2.10.0")
    freeDebugImplementation("com.mapbox.mapboxsdk:mapbox-android-navigation-ui:0.42.6")
    freeDebugImplementation("com.mapbox.mapboxsdk:mapbox-android-services:4.7.0")
    freeDebugImplementation ("com.mapbox.navigation:ui-dropin:2.10.0")

    implementation ("com.mapbox.mapboxsdk:mapbox-sdk-services:5.8.0")
    implementation ("com.mapbox.mapboxsdk:mapbox-sdk-turf:5.8.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.compose.material:material:1.4.3")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation ("androidx.activity:activity-compose:1.7.2")
    implementation(libs.material3)
    freeDebugImplementation(" 'com.mapbox.mapboxsdk:mapbox-sdk-services:7.0.0'")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-turf:6.8.0")
    implementation("com.mapbox.maps:android:11.5.1")
    implementation("androidx.room:room-runtime:2.3.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("org.osmdroid:osmdroid-android:6.1.11")
    implementation ("org.osmdroid:osmdroid-wms:6.1.11")
    implementation ("org.osmdroid:osmdroid-mapsforge:6.1.11")
    implementation ("com.github.MKergall:osmbonuspack:6.9.0")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.4.3")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.4.3")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:1.4.3")
    implementation("androidx.compose.material3:material3:1.1.0-alpha03")
    implementation("org.osmdroid:osmdroid-android:6.1.10")
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation (platform("com.google.firebase:firebase-bom:30.0.0"))
    implementation ("com.google.firebase:firebase-database-ktx")
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha10")
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation ("androidx.room:room-runtime:2.5.1")
    implementation ("androidx.room:room-ktx:2.5.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation(libs.androidx.appcompat)
    implementation ("com.google.android.material:material:1.8.0")

    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    kapt ("androidx.room:room-compiler:2.5.1")
    kapt ("androidx.room:room-compiler:2.5.1")

}

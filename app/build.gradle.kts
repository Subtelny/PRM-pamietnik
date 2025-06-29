plugins {
    id("com.google.devtools.ksp")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "pl.sjanda.jpamietnik"
    compileSdk = 35

    defaultConfig {
        applicationId = "pl.sjanda.jpamietnik"
        minSdk = 34
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.window.size.class1.android)
    implementation(libs.androidx.window)
    implementation(libs.androidx.material3.adaptive.navigation.suite.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.compose.material.iconsExtended)
    implementation("com.google.accompanist:accompanist-adaptive:0.26.2-beta")
    implementation(libs.androidx.compose.material3.adaptive.navigationSuite)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization.json)

    implementation("androidx.room:room-runtime:2.7.1")
    annotationProcessor("androidx.room:room-compiler:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    implementation("androidx.room:room-common:2.6.1")
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.accompanist.permissions)
}
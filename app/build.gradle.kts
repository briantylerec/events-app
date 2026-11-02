plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.redbull.lab.events"; compileSdk = 36
    defaultConfig {
        applicationId = "com.redbull.lab.events"; minSdk = 24; targetSdk = 35; versionCode =
        1; versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

kapt { correctErrorTypes = true }
dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui);
    implementation(libs.compose.material3);
    implementation(libs.compose.activity); implementation(libs.compose.preview)
    implementation(libs.lifecycle.viewmodel);
    implementation(libs.lifecycle.viewmodel.ktx);
    implementation(libs.lifecycle.runtime)
    implementation(libs.hilt.android);
    kapt(libs.hilt.compiler);
    kapt(libs.kotlin.metadata);
    implementation(libs.hilt.navigation)
    implementation(libs.shared.ui)
    implementation(libs.shared.sdk)
}

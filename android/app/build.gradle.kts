plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val mapsApiKey = providers.gradleProperty("ARKE_MAPS_API_KEY")
    .orElse(providers.environmentVariable("ARKE_MAPS_API_KEY"))
    .orElse("")

android {
    namespace = "dev.arke.app"
    compileSdk = 35
    defaultConfig {
        // Temporary development ID; store identity is not approved.
        applicationId = "dev.arke.app.dev"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.1-dev"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey.get()
        buildConfigField("boolean", "MAPS_CONFIGURED", mapsApiKey.get().isNotBlank().toString())
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":core"))
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("com.google.maps.android:maps-compose:6.4.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

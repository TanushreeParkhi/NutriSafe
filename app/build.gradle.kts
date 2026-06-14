plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.priveat.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.priveat.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        fun String.asBuildConfigString() = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

        val backendBaseUrl = providers.gradleProperty("PRIVEAT_BACKEND_BASE_URL").orElse("").get()
        val cloudAiEnabled = providers.gradleProperty("PRIVEAT_CLOUD_AI_ENABLED").orElse("false").get()
        val backendAuthEnabled = providers.gradleProperty("PRIVEAT_BACKEND_AUTH_ENABLED").orElse("false").get()
        val geminiFlashModel = providers.gradleProperty("GEMINI_FLASH_MODEL").orElse("gemini-3-flash-preview").get()
        val geminiProModel = providers.gradleProperty("GEMINI_PRO_MODEL").orElse("gemini-3.1-pro-preview").get()

        buildConfigField("String", "PRIVEAT_BACKEND_BASE_URL", backendBaseUrl.asBuildConfigString())
        buildConfigField("Boolean", "CLOUD_AI_ENABLED", cloudAiEnabled.toBooleanStrictOrNull()?.toString() ?: "false")
        buildConfigField("Boolean", "BACKEND_AUTH_ENABLED", backendAuthEnabled.toBooleanStrictOrNull()?.toString() ?: "false")
        buildConfigField("String", "GEMINI_FLASH_MODEL", geminiFlashModel.asBuildConfigString())
        buildConfigField("String", "GEMINI_PRO_MODEL", geminiProModel.asBuildConfigString())
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.sqlite:sqlite:2.4.0")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// Load local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "com.darach.plcards"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.darach.plcards"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.darach.plcards.HiltTestRunner" // For Hilt testing
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Inject Firebase API key from local.properties
        val apiKey = localProperties.getProperty("FIREBASE_API_KEY") ?: ""
        buildConfigField("String", "FIREBASE_API_KEY", "\"$apiKey\"")
        
        // Inject API Base URL from local.properties
        val baseUrl = localProperties.getProperty("BASE_URL")
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13" // Ensure compatibility
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
    
    // Process google-services.json to inject API key at build time
    applicationVariants.all {
        val variant = this
        variant.mergeResourcesProvider.get().doFirst {
            val googleServicesFile = file("google-services.json")
            if (googleServicesFile.exists()) {
                val fileContent = googleServicesFile.readText()
                val apiKey = localProperties.getProperty("FIREBASE_API_KEY") ?: ""
                val newContent = fileContent.replace("\${FIREBASE_API_KEY}", apiKey)
                googleServicesFile.writeText(newContent)
            }
        }
    }
}

dependencies {
    // Core & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.foundation)

    // Window Manager for responsive layouts
    implementation(libs.androidx.compose.material3.window.size)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Hilt (DI)
    implementation(libs.hilt.android)
    implementation(libs.firebase.crashlytics.ktx)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Networking (Retrofit & OkHttp)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Database (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // Paging 3 for pagination
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)

    // Image Loading (Coil 3)
    implementation(libs.coil.compose)
    implementation(libs.coil.network) // For OkHttp integration

    // Haze for backdrop effects
    implementation(libs.haze)
    implementation(libs.haze.materials)

    // Background Tasks (WorkManager)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Palette API for color extraction
    implementation(libs.androidx.palette.ktx)

    // SplashScreen
    implementation(libs.androidx.core.splashscreen)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(kotlin("test"))
}
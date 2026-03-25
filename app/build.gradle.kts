import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.simats.unimind"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.simats.unimind"
        minSdk = 35
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Optional: set in gradle.properties:
        // - GEMINI_API_KEYS=KEY1,KEY2,... (comma-separated, up to 6 keys for fallback)
        // - or GEMINI_API_KEY=single_key
        val geminiKey = project.findProperty("GEMINI_API_KEY") as String? ?: ""
        val geminiKeys = project.findProperty("GEMINI_API_KEYS") as String? ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        buildConfigField("String", "GEMINI_API_KEYS", "\"$geminiKeys\"")

        // Google Fitness / Google APIs key (stored in local.properties as FIT_API_KEY)
        val fitApiKey: String = run {
            val props = Properties()
            val file = project.rootProject.file("local.properties")
            if (file.exists()) {
                file.inputStream().use { props.load(it) }
            }
            props.getProperty("FIT_API_KEY", "")
        }
        buildConfigField("String", "FIT_API_KEY", "\"$fitApiKey\"")
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("androidx.biometric:biometric:1.1.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.android.billingclient:billing-ktx:7.1.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // AppCompat for DayNight theming and AppCompatActivity
    implementation("androidx.appcompat:appcompat:1.7.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}
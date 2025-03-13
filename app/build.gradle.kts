plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services") // Firebase Services
}

android {
    namespace = "com.app.gadjahdjaya"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.gadjahdjaya"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true // ✅ Menambahkan View Binding
    }
}

dependencies {
    // ✅ AndroidX & Material Design
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.androidx.constraintlayout)
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation ("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation ("com.google.code.gson:gson:2.8.8")



    // ✅ Firebase Dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-functions-ktx:20.4.0")
    implementation ("com.google.firebase:firebase-appcheck-playintegrity:16.0.0")


    // ✅ Midtrans Payment Gateway Dependencies
    implementation("com.midtrans:uikit:2.0.0-SANDBOX")

    // ✅ Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // ✅ Utilities
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // ✅ Navigation Component (Fragment & UI)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    // ✅ Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ✅ PieChart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.itextpdf:itext7-core:7.2.5")
}

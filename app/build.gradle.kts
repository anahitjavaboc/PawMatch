plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.anahit.pawmatch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.anahit.pawmatch"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Securely storing Firebase Test Credentials
        buildConfigField("String", "TEST_EMAIL", "\"${project.findProperty("TEST_EMAIL") ?: ""}\"")
        buildConfigField("String", "TEST_PASSWORD", "\"${project.findProperty("TEST_PASSWORD") ?: ""}\"")
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${project.property("CLOUDINARY_CLOUD_NAME")}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"${project.property("CLOUDINARY_API_KEY")}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${project.property("CLOUDINARY_API_SECRET")}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${project.property("CLOUDINARY_CLOUD_NAME")}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"${project.property("CLOUDINARY_API_KEY")}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${project.property("CLOUDINARY_API_SECRET")}\"")
        }
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0") // Updated
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.github.yuyakaido:cardstackview:2.3.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2") // Updated
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation("androidx.cardview:cardview:1.0.0") // Updated
    implementation("androidx.annotation:annotation:1.8.0")
    implementation("com.cloudinary:cloudinary-android:2.5.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")
    testImplementation("junit:junit:4.13.2") // Updated
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

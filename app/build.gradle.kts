plugins {
    id("com.android.application")
}

android {
    namespace = "cl.fotobragps.app"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    buildToolsVersion = "37.0.0"
    defaultConfig {
        applicationId = "cl.fotobragps.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 8
        versionName = "1.2.1"
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX estable vigente en 2026
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Última versión estable disponible de estas librerías
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.exifinterface:exifinterface:1.4.2")

    // Material Components estable 2026
    implementation("com.google.android.material:material:1.14.0")

    // CameraX estable agosto 2026
    val camerax = "1.6.2"
    implementation("androidx.camera:camera-core:$camerax")
    implementation("androidx.camera:camera-camera2:$camerax")
    implementation("androidx.camera:camera-lifecycle:$camerax")
    implementation("androidx.camera:camera-view:$camerax")

    // GPS estable junio 2026
    implementation("com.google.android.gms:play-services-location:21.4.0")
}

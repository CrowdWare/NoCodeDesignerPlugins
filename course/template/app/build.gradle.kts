import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "at.crowdware.coursereader"
    compileSdk = 35

    sourceSets {
        // Configure the `main` source set to include the custom directory
        getByName("main") {
            kotlin.srcDir(layout.buildDirectory.dir("generated/version"))
        }
    }

    defaultConfig {
        androidResources {
            ignoreAssetsPattern += listOf(
                "!.svn",
                "!.git",
                "!.gitignore",
                "!.ds_store",
                "!*.scc",
                "<dir>_*",
                "!CVS",
                "!thumbs.db",
                "!picasa.ini",
                "!*~"
            )
        }
        applicationId = "at.crowdware.coursekreader"
        minSdk = 29
        targetSdk = 35
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
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.navigation:navigation-compose:2.6.0")
    // rest client
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    // grammar for parsing SML
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")

    // video player
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    // youtube player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    // filament 3D
    implementation("com.google.android.filament:filament-android:1.54.5")
    implementation("com.google.android.filament:filament-utils-android:1.54.5")
    implementation("com.google.android.filament:gltfio-android:1.54.5")

    // 👇 NEU ab Kotlin 2.0 – explizit hinzufügen!
    implementation("androidx.compose.compiler:compiler:1.5.10")

    implementation(libs.androidx.material.icons.extended)
}
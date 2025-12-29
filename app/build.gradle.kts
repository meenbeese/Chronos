import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.meenbeese.chronos"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.meenbeese.chronos"
        minSdk = 26
        targetSdk = 36
        versionCode = 14
        versionName = "2025.3.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.5"
    }

    lint {
        abortOnError = false
    }

    androidResources {
        generateLocaleConfig = true
    }
}

kotlin {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        jvmTarget.set(JvmTarget.JVM_17)
    }

    sourceSets.all {
        languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        languageSettings.optIn("kotlin.time.ExperimentalTime")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.navigation:navigation-compose:2.9.6")

    implementation("androidx.compose.ui:ui:1.9.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.3")
    implementation("androidx.compose.animation:animation:1.9.3")
    implementation("androidx.compose.foundation:foundation:1.9.3")
    implementation("androidx.compose.runtime:runtime:1.9.3")
    implementation("androidx.compose.runtime:runtime-livedata:1.9.3")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    debugImplementation("androidx.compose.ui:ui-tooling:1.9.3")

    implementation("androidx.media3:media3-exoplayer:1.9.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.9.0")

    ksp("androidx.room:room-compiler:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    implementation("androidx.room:room-runtime:2.8.4")

    implementation("com.github.skydoves:colorpicker-compose:1.1.3")
    implementation("com.github.alexdametto:compose-emoji-picker:1.0.0")

    implementation("io.arrow-kt:arrow-core:2.2.1.1")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.insert-koin:koin-androidx-compose:4.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
}

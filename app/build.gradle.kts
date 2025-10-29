// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application) // 使用 alias 引用 Android 插件
    alias(libs.plugins.jetbrains.kotlin.android) // 使用 alias 引用 Kotlin 插件，注意别名是 jetbrains-kotlin-android
    // ...
}

android {
    namespace = "com.example.insightnewsandroid"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.insightnewsandroid"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0") // 选择一个较新的稳定版本
}
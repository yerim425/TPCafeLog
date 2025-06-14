import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.yrlee.tpcafelog"
    compileSdk = 35

    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").inputStream())

    defaultConfig {
        applicationId = "com.yrlee.tpcafelog"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "KAKAO_NATIVE_API_KEY", properties["KAKAO_NATIVE_API_KEY"].toString())
        buildConfigField("String", "KAKAO_REST_API_KEY", properties["KAKAO_REST_API_KEY"].toString())
        buildConfigField("String", "NAVER_CLIENT_ID", properties["NAVER_CLIENT_ID"].toString())
        buildConfigField("String", "NAVER_CLIENT_SECRET", properties["NAVER_CLIENT_SECRET"].toString())
        manifestPlaceholders["KAKAO_NATIVE_API_KEY"] = properties["KAKAO_NATIVE_API_KEY"] as String
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

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation("com.kakao.maps.open:android:2.12.8")
    implementation("com.kakao.sdk:v2-user:2.17.0") // 로그인
    implementation("com.kakao.sdk:v2-share:2.17.0") // 카톡 공유 (선택)
    implementation(libs.glide)
    implementation(libs.converter.scalars)
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.converter.gson)
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.tbuonomo:dotsindicator:5.1.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mogu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mogu"
        minSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.kakao.maps.open:android:2.6.0")
    implementation("com.kakao.sdk:kakaomap:2.6.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // UI 관련 라이브러리
    implementation("me.relex:circleindicator:2.1.6") // Circle Indicator
    implementation("androidx.recyclerview:recyclerview:1.3.2") // RecyclerView
    implementation(libs.appcompat) // AppCompat 라이브러리
    implementation(libs.material) // Material 디자인 라이브러리
    implementation(libs.activity) // Activity KTX 라이브러리
    implementation(libs.constraintlayout) // ConstraintLayout
    implementation("androidx.viewpager2:viewpager2:1.0.0") // ViewPager2

    // 이미지 로딩 라이브러리
    implementation("com.github.bumptech.glide:glide:4.11.0")
    implementation(libs.play.services.maps) // Glide
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0") // Glide Annotation Processor

    // 네트워크 관련 라이브러리
    implementation("org.java-websocket:Java-WebSocket:1.5.2") // WebSocket
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Gson Converter

    // 카카오 로그인 API
    implementation("com.kakao.sdk:v2-user:2.20.1") // 카카오 로그인 API 모듈

    // 테스트 라이브러리
    testImplementation(libs.junit) // JUnit
    androidTestImplementation(libs.ext.junit) // AndroidX JUnit
    androidTestImplementation(libs.espresso.core) // Espresso

}

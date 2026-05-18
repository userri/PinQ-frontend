import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// local.properties 에서 앱키 읽기
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.reader(Charsets.UTF_8))
}

android {
    namespace = "com.example.pinq_frontend"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pinq_frontend"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 카카오 네이티브 앱키 (local.properties: kakao.native.app.key=YOUR_KEY)
        val kakaoKey = localProps.getProperty("kakao.native.app.key", "")
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoKey\"")
        manifestPlaceholders["kakaoNativeAppKey"] = kakaoKey

        // 구글 웹 클라이언트 ID (local.properties: google.web.client.id=YOUR_ID)
        val googleClientId = localProps.getProperty("google.web.client.id", "")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleClientId\"")

        // 백엔드 BASE_URL (local.properties: base.url=http://192.168.x.x:8080/)
        // 미설정 시 adb reverse 방식 기본값 사용
        val baseUrl = localProps.getProperty("base.url", "http://localhost:8080/")
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // release 빌드 시 필수 키가 비어 있으면 런타임 크래시 대신 빌드 시점에 차단
            val kakaoKeyForCheck = localProps.getProperty("kakao.native.app.key", "")
            val googleIdForCheck  = localProps.getProperty("google.web.client.id", "")
            if (kakaoKeyForCheck.isBlank()) {
                error("release 빌드에 kakao.native.app.key 가 설정되지 않았습니다. local.properties 를 확인하세요.")
            }
            if (googleIdForCheck.isBlank()) {
                error("release 빌드에 google.web.client.id 가 설정되지 않았습니다. local.properties 를 확인하세요.")
            }
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.core)
    implementation(libs.moshi.kotlin)

    // 카카오 로그인
    implementation(libs.kakao.user)

    // 구글 로그인 (Credential Manager — 최신 권장 방식)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.google.id)

    // DataStore (JWT 토큰 영구 저장)
    implementation(libs.datastore.preferences)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
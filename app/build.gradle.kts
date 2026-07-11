import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services) apply false
}

// google-services.json 이 있을 때만 플러그인 적용 — 파일을 아직 받지 않아도 빌드는 통과한다.
// 단, 플러그인 미적용 상태에서는 FCM 초기화가 안 되므로 푸시알림이 동작하지 않는다.
// Firebase 콘솔 → 프로젝트 설정 → Android 앱(com.finq.app) → google-services.json 을 app/ 에 배치할 것.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.warn("WARNING: app/google-services.json 이 없어 google-services 플러그인을 건너뜁니다 — FCM 푸시가 동작하지 않습니다.")
}

// local.properties 에서 앱키 읽기
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.reader(Charsets.UTF_8))
}

// 카카오 네이티브 앱키 (local.properties: kakao.native.app.key=YOUR_KEY)
val kakaoKey = localProps.getProperty("kakao.native.app.key", "")
// 구글 웹 클라이언트 ID (local.properties: google.web.client.id=YOUR_ID)
val googleClientId = localProps.getProperty("google.web.client.id", "")
// 개발용 BASE_URL (local.properties: base.url=http://192.168.x.x:8080/)
// 미설정 시 adb reverse 방식 기본값 사용
val debugBaseUrl = localProps.getProperty("base.url", "http://localhost:8080/")
// 운영 BASE_URL — 반드시 https. 재정의: local.properties 의 base.url.release
// 2026-07: finq.duckdns.org → yuri-hub.com 이전 (DuckDNS 네임서버 불안정으로 인한 간헐적 타임아웃 회피).
// 같은 서버, Let's Encrypt 인증서 적용됨. 구 도메인도 당분간 병행 서빙되므로 구버전 클라이언트는 계속 동작한다.
val releaseBaseUrl = localProps.getProperty("base.url.release", "https://yuri-hub.com/")

// 업로드 키스토어 (local.properties 에 설정 — 절대 git 에 커밋 금지):
//   keystore.path=pinq-upload.jks           (repo 루트 기준 상대경로 또는 절대경로)
//   keystore.store.password=...
//   keystore.key.alias=pinq
//   keystore.key.password=...
// 키스토어 생성:
//   keytool -genkeypair -v -keystore pinq-upload.jks -alias pinq \
//     -keyalg RSA -keysize 2048 -validity 10000
val keystorePath = localProps.getProperty("keystore.path", "")
val keystoreFile = keystorePath.takeIf { it.isNotBlank() }?.let { p ->
    val f = File(p)
    if (f.isAbsolute) f else rootProject.file(p)
}
val hasKeystore = keystoreFile?.exists() == true

android {
    namespace = "com.finq.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.finq.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 8
        versionName = "1.1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoKey\"")
        manifestPlaceholders["kakaoNativeAppKey"] = kakaoKey
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleClientId\"")
    }

    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = keystoreFile
                storePassword = localProps.getProperty("keystore.store.password", "")
                keyAlias = localProps.getProperty("keystore.key.alias", "")
                keyPassword = localProps.getProperty("keystore.key.password", "")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
            // 구글 공식 테스트 배너 광고 단위 — 개발 중 실광고 클릭으로 인한 계정 제재 방지
            buildConfigField(
                "String", "ADMOB_BANNER_UNIT_ID",
                "\"ca-app-pub-3940256099942544/9214589741\""
            )
        }
        release {
            buildConfigField("String", "BASE_URL", "\"$releaseBaseUrl\"")
            // FinQ 실제 배너 광고 단위 (정답화면_하단배너)
            buildConfigField(
                "String", "ADMOB_BANNER_UNIT_ID",
                "\"ca-app-pub-4958708999777591/9464900396\""
            )
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
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

// 릴리즈 필수 설정 검증 — configuration 단계가 아닌 release 빌드 실행 시에만 실패한다.
// (예전에는 buildTypes.release {} 안에서 error() 를 던져 debug 빌드/gradle sync 까지 깨졌음)
val validateReleaseConfig = tasks.register("validateReleaseConfig") {
    doFirst {
        val problems = buildList {
            if (kakaoKey.isBlank())
                add("kakao.native.app.key 가 설정되지 않았습니다.")
            if (googleClientId.isBlank())
                add("google.web.client.id 가 설정되지 않았습니다.")
            if (!releaseBaseUrl.startsWith("https://"))
                add("release BASE_URL 은 https:// 로 시작해야 합니다. (현재: $releaseBaseUrl — base.url.release 로 재정의 가능)")
            if (!hasKeystore)
                add("업로드 키스토어가 없습니다. keytool 로 생성 후 keystore.* 항목을 설정하세요 (파일 상단 주석 참고). 없으면 서명 안 된 AAB 가 만들어져 Play Console 이 거부합니다.")
        }
        if (problems.isNotEmpty()) {
            throw GradleException(
                "릴리즈 빌드 검증 실패 — local.properties 를 확인하세요:\n" +
                    problems.joinToString("\n") { " - $it" }
            )
        }
    }
}
tasks.configureEach {
    if (name == "preReleaseBuild") dependsOn(validateReleaseConfig)
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

    // AdMob 배너 광고
    implementation(libs.play.services.ads)

    // FCM 푸시알림
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
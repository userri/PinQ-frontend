# PinQ R8/ProGuard 규칙.
#
# 이 프로젝트는 Moshi 를 리플렉션 기반 KotlinJsonAdapterFactory 로 사용한다
# (NetworkModule.kt). 리플렉션은 클래스/생성자/프로퍼티 이름에 의존하므로
# JSON 을 오가는 DTO 는 난독화·제거 대상에서 제외해야 한다.
# 규칙이 빠지면 릴리즈 빌드에서 모든 API 응답 파싱이 런타임에 실패한다.

# ── 크래시 스택트레이스 해독용 (Play Console 비정상 종료 리포트) ──
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# ── Retrofit ──
# 제네릭 시그니처·어노테이션은 Retrofit 이 런타임에 읽는다
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ── Moshi (리플렉션 기반 KotlinJsonAdapterFactory) ──
# kotlin-reflect 가 @Metadata 로 Kotlin 생성자/프로퍼티를 찾는다
-keep class kotlin.Metadata { *; }
# JSON 직렬화 대상 DTO/모델 — 필드·생성자 이름이 JSON 키와 일치해야 하므로 전부 유지
-keep class com.finq.app.data.remote.dto.** { *; }
-keep class com.finq.app.data.model.** { *; }

# 카카오 SDK, Credential Manager(Google 로그인), OkHttp, Compose 는
# 각 라이브러리가 AAR 에 동봉한 consumer rules 로 처리된다.

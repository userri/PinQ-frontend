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

# ── WorkManager / Room (play-services-ads 가 끌어오는 전이 의존성) ──
# Room 은 DB 구현 클래스를 "클래스명 + _Impl" 문자열 리플렉션으로 찾는다.
# R8 이 이름을 바꾸면 매칭이 깨져 앱 시작 즉시 크래시한다
# ("Failed to create an instance of androidx.work.impl.WorkDatabase" —
#  v1.1 스토어 설치판에서 실제 발생, 폰 crash 로그로 확인).
-keep class androidx.work.** { *; }
-keepnames class * extends androidx.room.RoomDatabase

# ── Kakao SDK (내부적으로 Gson 리플렉션 사용) ──
# Gson 이 enum 상수를 Class.getField(이름) 로 찾는데 R8 full mode 가
# enum 필드를 제거해 시작 시 크래시했다 (NoSuchFieldException: TokenNotFound
# — ClientErrorCause enum, v1.1.1 검증 중 에뮬레이터에서 확인).
# 카카오 공식 가이드 규칙:
-keep class com.kakao.sdk.**.model.* { <fields>; }
-keep class * extends com.google.gson.TypeAdapter

# ── enum 리플렉션 전역 보호 ──
# Gson/Moshi 모두 enum 을 리플렉션으로 다루므로 모든 enum 의
# values()/valueOf()/필드를 유지한다 (우리 Category enum 포함).
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}

# 카카오 SDK, Credential Manager(Google 로그인), OkHttp, Compose 는
# 각 라이브러리가 AAR 에 동봉한 consumer rules 로 처리된다.

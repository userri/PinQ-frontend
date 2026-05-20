package com.finq.app.data.remote

import com.finq.app.data.remote.dto.AnswerApiRequest
import com.finq.app.data.remote.dto.AnswerApiResponse
import com.finq.app.data.remote.dto.QuizApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 백엔드 QuizController 와 매핑되는 Retrofit 서비스.
 *
 * Retrofit 이 이 인터페이스의 어노테이션을 보고 자동으로 HTTP 요청 구현체를 만들어준다.
 *  - @GET / @POST: HTTP 메서드
 *  - @Path: URL path 파라미터 치환 ({quizId})
 *  - @Body: JSON 요청 바디 (Moshi 가 직렬화)
 *  - suspend: Coroutine 으로 비동기 호출. 응답이 도착할 때까지 호출 코루틴이 일시중단되고,
 *             스레드는 차단되지 않는다 (OkHttp 내부 디스패처가 처리).
 */
interface QuizApi {

    @GET("api/quizzes/today")
    suspend fun getTodayQuizzes(): List<QuizApiResponse>

    @POST("api/quizzes/{quizId}/answer")
    suspend fun submitAnswer(
        @Path("quizId") quizId: Long,
        @Body request: AnswerApiRequest,
    ): AnswerApiResponse
}

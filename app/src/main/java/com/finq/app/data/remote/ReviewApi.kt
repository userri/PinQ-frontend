package com.finq.app.data.remote

import com.finq.app.data.remote.dto.GardenApiResponse
import com.finq.app.data.remote.dto.ReviewAnswerApiRequest
import com.finq.app.data.remote.dto.ReviewAnswerApiResponse
import com.finq.app.data.remote.dto.ReviewsTodayApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 오답 복습("잔디에 물 주기").
 *
 * 복습 결과는 스트릭·정답률 통계에 반영되지 않는다(백엔드 설계).
 */
interface ReviewApi {

    @GET("api/reviews/today")
    suspend fun getTodayReviews(): ReviewsTodayApiResponse

    @POST("api/reviews/{quizId}/answer")
    suspend fun submitReviewAnswer(
        @Path("quizId") quizId: Long,
        @Body request: ReviewAnswerApiRequest,
    ): ReviewAnswerApiResponse

    @GET("api/reviews/garden")
    suspend fun getGarden(): GardenApiResponse
}

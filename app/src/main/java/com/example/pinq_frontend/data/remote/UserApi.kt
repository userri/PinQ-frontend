package com.example.pinq_frontend.data.remote

import com.example.pinq_frontend.data.remote.dto.UserStatsApiResponse
import retrofit2.http.GET

interface UserApi {

    @GET("api/users/me/stats")
    suspend fun getUserStats(): UserStatsApiResponse
}

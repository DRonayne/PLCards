package com.darach.plcards.data.remote

import com.darach.plcards.BuildConfig
import com.darach.plcards.data.remote.dto.CardApiResponse
import retrofit2.http.GET

interface ApiService {
    @GET("api/cards")
    suspend fun getAllCards(): CardApiResponse

    companion object {
        val BASE_URL = BuildConfig.BASE_URL
    }
}
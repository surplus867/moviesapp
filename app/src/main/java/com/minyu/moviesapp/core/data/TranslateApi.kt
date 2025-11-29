package com.minyu.moviesapp.core.data

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class TranslateRequest(val q: String, val source: String, val target: String, val format: String = "text")
data class TranslateResponse(val translatedText: String)

interface TranslateApi {
    @Headers("Content-Type: application/json")
    @POST("/translate")
    suspend fun translate(@Body req: TranslateRequest): TranslateResponse
}
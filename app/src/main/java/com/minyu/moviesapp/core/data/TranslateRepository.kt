package com.minyu.moviesapp.core.data

interface TranslateRepository {
    suspend fun translate(text: String, targetLang: String): String
}
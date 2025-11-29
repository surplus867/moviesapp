package com.minyu.moviesapp.core.data

import javax.inject.Inject

class TranslateRepositoryImpl @Inject constructor(

) : TranslateRepository {
    override suspend fun translate(text: String, targetLang: String): String {
        return try {
            when {
                targetLang.startsWith("zn", ignoreCase = true) -> "(中文翻译) $text"
                targetLang.startsWith("ja", ignoreCase = true) -> "(日本語翻訳) $text"
                targetLang.startsWith("ko", ignoreCase = true) -> "(한국어 번역) $text"
                else -> text
            }
        } catch (t: Throwable) {
            text
        }
    }
}
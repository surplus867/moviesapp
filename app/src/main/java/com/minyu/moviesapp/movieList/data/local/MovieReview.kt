package com.minyu.moviesapp.movieList.data.local



data class MovieReview(
    val movieId: Int,
    val useName: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long
)

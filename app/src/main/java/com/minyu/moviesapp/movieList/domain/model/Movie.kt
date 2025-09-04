package com.minyu.moviesapp.movieList.domain.model

import com.minyu.moviesapp.movieList.data.remote.respond.TrailerDto

data class Movie(
    val adult: Boolean,
    val backdrop_path: String,
    val genre_ids: List<Int>,
    val original_language: String,
    val original_title: String,
    val country: String,
    val overview: String,
    val popularity: Double,
    val dateAdded: Long,
    val poster_path: String,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int,
    val id: Int,
    val category: String,
    val region: String,
    val trailers: List<TrailerDto> = emptyList()
)
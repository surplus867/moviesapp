package com.minyu.moviesapp.movieList.domain.model

import com.minyu.moviesapp.movieList.data.remote.respond.TrailerDto

data class Movie(
    val adult: Boolean,
    val backdropPath: String,
    val genreIds: List<Int>,
    val originalLanguage: String,
    val originalTitle: String,
    val country: String,
    val overview: String,
    val popularity: Double,
    val dateAdded: Long,
    val posterPath: String,
    val releaseDate: String,
    val title: String,
    val video: Boolean,
    val voteAverage: Double,
    val voteCount: Int,
    val id: Int,
    val category: String,
    val region: String,
    val trailers: List<TrailerDto> = emptyList(),
    val director: String? = null,
    val topCast: List<CastMember> = emptyList()
)

data class CastMember(
    val name: String,
    val character: String,
    val profilePath: String?
)
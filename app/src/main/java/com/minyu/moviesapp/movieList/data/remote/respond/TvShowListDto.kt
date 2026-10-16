package com.minyu.moviesapp.movieList.data.remote.respond

import com.google.gson.annotations.SerializedName

data class TvShowListDto(
    val page: Int = 1,
    val results: List<TvShowDto> = emptyList()
)

data class TvShowDto(
    val id: Int,
    val name: String = "",
    val overview: String = "",
    val popularity: Double = 0.0,
    @SerializedName("original_name") val originalName: String = "",
    @SerializedName("original_language") val originalLanguage: String = "",
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0
)
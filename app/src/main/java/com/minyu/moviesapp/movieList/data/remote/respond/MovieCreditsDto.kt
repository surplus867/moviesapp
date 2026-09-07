package com.minyu.moviesapp.movieList.data.remote.respond

data class MovieCreditsDto(
    val cast: List<CastDto> = emptyList(),
    val crew: List<CrewDto> = emptyList()
)
data class CastDto(
    val id: Int,
    val name: String,
    val character: String? = null,
    val order: Int? = null
)
data class CrewDto(
    val id: Int,
    val name: String,
    val job: String? = null,
    val department: String? = null
)
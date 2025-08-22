package com.minyu.moviesapp.movieList.data.remote.respond

// Data class representing the response for a list of trailers from the API
data class TrailerListDto(
    val results: List<TrailerDto> // List of trailer DTOs
)
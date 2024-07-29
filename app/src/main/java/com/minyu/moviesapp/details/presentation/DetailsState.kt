package com.minyu.moviesapp.details.presentation

import com.minyu.moviesapp.movieList.domain.model.Movie

data class DetailsState(
    val isLoading: Boolean = false,
    val movie: Movie? = null,
)

package com.minyu.moviesapp.details.presentation

import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.domain.model.WatchProviderInfo

data class DetailsState(
    val isLoading: Boolean = false,
    val movie: Movie? = null,
    // Regional streaming-provider metadata shown in the "Where to Watch" section.
    val watchProviderInfo: WatchProviderInfo? = null,
)

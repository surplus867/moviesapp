package com.minyu.moviesapp.movieList.presentation


sealed interface MovieListUiEvent{
    data class Paginate(val category: String): MovieListUiEvent
    object Navigate: MovieListUiEvent
    // Discovery controls events
    data class UpdateSearchQuery(val query: String) : MovieListUiEvent
    data class UpdateYearFilter(val year: String?) : MovieListUiEvent
    data class UpdateLanguageFilter(val language: String?) : MovieListUiEvent
    data class UpdateGenreFilter(val genre: String?) : MovieListUiEvent
    object ClearFilters : MovieListUiEvent
}
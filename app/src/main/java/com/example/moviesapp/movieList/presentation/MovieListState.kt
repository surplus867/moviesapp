package com.example.moviesapp.movieList.presentation

import com.example.moviesapp.movieList.domain.model.Movie

data class MovieListState(
    val isLoading: Boolean = false, // Indicates whether the data is currently being loaded

    val popularMovieListPage: Int = 1, // Current page number for popular movie list pagination
    val upcomingMovieListPage: Int = 1, // Current page number for upcoming movie list pagination

    val isCurrentPopularScreen: Boolean = true, // Indicates whether the current screen is the popular movie screen

    val popularMovieList: List<Movie> = emptyList(), // List of popular movies

    val upcomingMovieList: List<Movie> = emptyList() // List of upcoming movies
)

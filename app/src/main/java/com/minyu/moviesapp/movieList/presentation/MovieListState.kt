package com.minyu.moviesapp.movieList.presentation

import com.minyu.moviesapp.movieList.domain.model.Movie

data class MovieListState(
    val isLoading: Boolean = false, // Indicates whether the data is currently being loaded

    val popularMovieListPage: Int = 1, // Current page number for popular movie list pagination
    val upcomingMovieListPage: Int = 1, // Current page number for upcoming movie list pagination

    val isCurrentPopularScreen: Boolean = true, // Indicates whether the current screen is the popular movie screen

    val popularMovieList: List<Movie> = emptyList(), // List of popular movies

    val upcomingMovieList: List<Movie> = emptyList(), // List of upcoming movies

    // Discovery inputs from the search and filter controls.
    val searchQuery: String = "",
    val selectedYear: String? = null,
    val selectedLanguage: String? = null,
    val selectedGenre: String? = null,

    // Dynamic filter options derived from the currently loaded movie lists.
    val availableYears: List<String> = emptyList(),
    val availableLanguages: List<String> = emptyList(),
    val availableGenres: List<String> = emptyList(),

    // Derived presentation lists for discovery and recommendation sections.
    val filteredPopularMovieList: List<Movie> = emptyList(),
    val filteredUpcomingMovieList: List<Movie> = emptyList(),
    val recommendedMovies: List<Movie> = emptyList()
)

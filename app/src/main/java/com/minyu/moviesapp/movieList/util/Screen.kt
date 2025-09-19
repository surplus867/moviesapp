package com.minyu.moviesapp.movieList.util

sealed class Screen(val route: String) {
    object Home: Screen("main")
    object PopularMovieList: Screen("popularMovie")
    object UpcomingMovieList: Screen("upcomingMovie")

    object AsianMovieList: Screen("asianMovie")

    object AsianDramaList: Screen("asianDrama")
    object Details: Screen("details")
}

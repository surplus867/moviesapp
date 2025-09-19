// AsianMovieState.kt
// This file defines the state and ViewModel for displaying Asian movies in the app.
// It handles fetching Asian movies, filtering by language/country, and adding movies to favorites.

package com.minyu.moviesapp.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.domain.repository.FavoriteMovieRepository
import com.minyu.moviesapp.movieList.domain.repository.MovieListRepository
import com.minyu.moviesapp.movieList.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Holds the UI state for the Asian movie screen
// isLoading: true if movies are being loaded
// error: error message if loading fails
// movies: list of Asian movies to display

data class AsianMovieState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val movies: List<Movie> = emptyList()
)

// ViewModel for Asian movies
// Injects repositories for movie list and favorites
@HiltViewModel
class AsianMovieViewModel @Inject constructor(
    private val movieListRepository: MovieListRepository,
    private val favoriteMovieRepository: FavoriteMovieRepository
) : ViewModel() {
    // Backing state for the UI
    private val _state = MutableStateFlow(AsianMovieState())
    val state: StateFlow<AsianMovieState> = _state

    // Fetch Asian movies from the repository
    // Filters movies by language (Korean, Chinese, Japanese) or country (KR, CN, JP)
    fun fetchAsianMovies() {
        val asianLanguages = listOf("ko", "zh", "ja") // Supported Asian languages
        val asianCountries = listOf("KR", "CN", "JP") // Supported Asian countries
        viewModelScope.launch {
            movieListRepository.getAsianMovieList(forceFetchFromRemote = false, page = 1)
                .collect { resource ->
                    if (resource is Resource.Success && resource.data != null) {
                        // Filter movies by language or country
                        val asianMovies = resource.data.filter {
                            it.original_language in asianLanguages || it.country in asianCountries
                        }
                        _state.value = AsianMovieState(movies = asianMovies)
                    }
                }
        }
    }

    // Add a movie to the favorites repository
    fun addToFavorites(movie: Movie) {
        viewModelScope.launch {
            favoriteMovieRepository.addFavorite(
                movieId = movie.id,
                title = movie.title,
                posterUrl = movie.poster_path,
                overview = movie.overview
            )
        }
    }
}
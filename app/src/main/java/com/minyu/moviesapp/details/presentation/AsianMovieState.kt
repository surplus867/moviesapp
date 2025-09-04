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


data class AsianMovieState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val movies: List<Movie> = emptyList()
)

@HiltViewModel
class AsianMovieViewModel @Inject constructor(
    private val movieListRepository: MovieListRepository,
    private val favoriteMovieRepository: FavoriteMovieRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AsianMovieState())
    val state: StateFlow<AsianMovieState> = _state

    fun fetchAsianMovies() {
        val asianLanguages = listOf("ko", "zh", "ja")
        val asianCountries = listOf("KR", "CN", "JP")
        viewModelScope.launch {
            movieListRepository.getAsianMovieList(forceFetchFromRemote = false, page = 1)
                .collect { resource ->
                    if (resource is Resource.Success && resource.data != null) {
                        val asianMovies = resource.data.filter {
                            it.original_language in asianLanguages || it.country in asianCountries
                        }
                        _state.value = AsianMovieState(movies = asianMovies)
                    }
                }
        }
    }

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
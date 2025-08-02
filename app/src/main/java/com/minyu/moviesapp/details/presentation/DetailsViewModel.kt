package com.minyu.moviesapp.details.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minyu.moviesapp.movieList.domain.repository.FavoriteMovieRepository
import com.minyu.moviesapp.movieList.domain.repository.MovieListRepository
import com.minyu.moviesapp.movieList.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val movieListRepository: MovieListRepository,
    private val favoriteMovieRepository: FavoriteMovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve the movie ID from SavedStateHandle
    private val movieId = savedStateHandle.get<Int>("movieId")

    // Internal mutable state flow for managing details screen state
    private var _detailsState = MutableStateFlow(DetailsState())

    // Public read-only state flow to expose details screen state
    val detailsState = _detailsState.asStateFlow()

    // Initialization block, called when the viewModel is created
    init {
        // Start fetching movie details upon ViewModel creation
        getMovie(movieId ?: -1)
    }

    // Function to fetch movie details
    private fun getMovie(id: Int) {
        viewModelScope.launch {
            _detailsState.update {
                it.copy(isLoading = true)
            }

            movieListRepository.getMovie(id).collectLatest { result ->
                when (result) {
                    is Resource.Error -> {
                        _detailsState.update {
                            it.copy(isLoading = false)
                        }
                    }

                    is Resource.Loading -> {
                        _detailsState.update {
                            it.copy(isLoading = result.isLoading)
                        }
                    }

                    is Resource.Success -> {
                        result.data?.let { movie ->
                            _detailsState.update {
                                it.copy(movie = movie, isLoading = false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun addFavoriteMovie(movieId: Int, title: String, posterUrl: String) {
        viewModelScope.launch {
            // Assuming you have a FavoriteMovieRepository injected
            favoriteMovieRepository.addFavorite(
                movieId = movieId,
                title = title,
                posterUrl = posterUrl
            )
        }
    }
}
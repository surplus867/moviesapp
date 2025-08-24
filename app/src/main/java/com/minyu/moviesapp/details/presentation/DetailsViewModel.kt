package com.minyu.moviesapp.details.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity
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

    private val _reviews = MutableStateFlow<List<MovieReviewEntity>>(emptyList())
    val reviews = _reviews.asStateFlow()

    // Initialization block, called when the viewModel is created
    init {
        // Start fetching movie details upon ViewModel creation
        getMovie(movieId ?: -1)
        movieId?.let { id ->
            viewModelScope.launch {
                movieListRepository.getReviewsForMovie(id).collectLatest { reviewsList ->
                    _reviews.value = reviewsList
                }
            }
    }
}

    // Function to fetch movie details
    private fun getMovie(id: Int) {
        viewModelScope.launch {
            // Set loading state
            _detailsState.update { it.copy(isLoading = true) }

            // Collect movie data from repository
            movieListRepository.getMovie(id).collect { result ->
                when (result) {
                    is Resource.Error -> {
                        // On error, stop loading
                        _detailsState.update {
                            it.copy(isLoading = false)
                        }
                    }
                    is Resource.Loading -> {
                        // Update loading state
                        _detailsState.update {
                            it.copy(isLoading = result.isLoading)
                        }
                    }
                    is Resource.Success -> {
                        result.data?.let { movie ->
                            // Try to fetch the offical trailer
                            val trailers = try {
                                movieListRepository.getMovieTrailers(id)
                                    .firstOrNull { it.official == true && it.type == "Trailer" }
                                    ?.let { listOf(it) } ?: emptyList()
                        } catch (e: Exception) {
                            emptyList()
                        }
                            // Update state with movie and trailers
                            val movieWithTrailers = movie.copy(trailers = trailers)
                            _detailsState.update {
                                it.copy(movie = movieWithTrailers, isLoading = false)
                            }
                        } ?: _detailsState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    // Add the current movie to favorites
    fun addFavoriteMovie(movieId: Int, title: String, posterUrl: String) {
        viewModelScope.launch {
            val overview = detailsState.value.movie?.overview ?: ""
            favoriteMovieRepository.addFavorite(
                movieId = movieId,
                title = title,
                posterUrl = posterUrl,
                overview = overview
            )
        }
    }

    fun insertReview(review: MovieReviewEntity) {
        viewModelScope.launch {
            movieListRepository.insertReview(review)
        }
    }

    fun deleteReview(reviewId: Int) {
        viewModelScope.launch {
            movieListRepository.deleteReview(reviewId)
        }
    }
}
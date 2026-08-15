package com.minyu.moviesapp.details.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity
import com.minyu.moviesapp.movieList.domain.repository.MovieListRepository
import com.minyu.moviesapp.movieList.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val movieListRepository: MovieListRepository,
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

    // Loads details payload and enriches it with trailer + watch-provider actions.
    private fun getMovie(id: Int) {
        viewModelScope.launch {
            _detailsState.update { it.copy(isLoading = true) }
            movieListRepository.getMovie(id).collect { result ->
                when (result) {
                    is Resource.Error -> {
                        _detailsState.update { it.copy(isLoading = false) }
                    }
                    is Resource.Loading -> {
                        _detailsState.update { it.copy(isLoading = result.isLoading) }
                    }
                    is Resource.Success -> {
                        result.data?.let { movie ->
                            // Keep network lookups together so the UI receives one coherent details state.
                            val trailers = movieListRepository.getMovieTrailers(id)
                            // Use device region for provider lookup; repository handles fallback when unavailable.
                            val region = Locale.getDefault().country.takeIf { it.isNotBlank() } ?: "US"
                            val watchProviderInfo = movieListRepository.getWatchProviders(id, region)
                            // Persist both watch actions: trailer playback + where-to-watch providers.
                            _detailsState.update {
                                it.copy(
                                    movie = movie.copy(trailers = trailers),
                                    watchProviderInfo = watchProviderInfo,
                                    isLoading = false
                                )
                            }
                        } ?: _detailsState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    fun insertReview(review: MovieReviewEntity) {
        viewModelScope.launch {
            movieListRepository.insertReview(review)
        }
    }
}
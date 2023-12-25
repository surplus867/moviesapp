package com.example.moviesapp.details.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesapp.movieList.domain.repository.MovieListRepository
import com.example.moviesapp.movieList.util.Resource
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
            // Update state to indicate that data is being loaded
            _detailsState.update {
                it.copy(isLoading = true)
            }

            // Fetch movie details from the repository using a flow
            movieListRepository.getMovie(id).collectLatest { result ->
                when (result) {
                    // Handle error case
                    is Resource.Error -> {
                        _detailsState.update {
                            // Update state to indicate that loading has stopped
                            it.copy(isLoading = false)
                        }
                    }

                    // Handle loading state
                    is Resource.Loading -> {
                        _detailsState.update {
                            it.copy(isLoading = result.isLoading)
                        }
                    }

                    // Handle successful data retrieval
                    is Resource.Success -> {
                        result.data?.let { movie ->
                            _detailsState.update {
                                // Update state with the fetched movie details
                                it.copy(movie = movie, isLoading = false)
                            }
                        }
                    }
                }
            }
        }
    }
}
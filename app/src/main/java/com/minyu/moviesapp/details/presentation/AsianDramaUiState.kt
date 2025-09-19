// AsianDramaUiState.kt
// This file defines the UI state and ViewModel for displaying Asian dramas in the app.
// It manages loading, error handling, and data retrieval for Asian drama content.

package com.minyu.moviesapp.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.domain.repository.MovieListRepository
import com.minyu.moviesapp.movieList.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class representing the UI state for Asian dramas
// dramas: List of Asian drama movies to display
// isLoading: True if data is currently being loaded
// error: Error message if loading fails

data class AsianDramaUiState(
    val dramas: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel for Asian drama screen
// Uses Hilt for dependency injection of the repository
@HiltViewModel
class AsianDramaViewModel @Inject constructor(
    private val repository: MovieListRepository
) : ViewModel() {
    // Backing property for UI state
    private val _uiState = MutableStateFlow(AsianDramaUiState())
    // Exposed immutable state for the UI to observe
    val uiState: StateFlow<AsianDramaUiState> = _uiState

    // Automatically load Asian dramas when ViewModel is created
    init {
        loadAsianDramas()
    }

    // Loads Asian dramas from the repository and updates UI state
    fun loadAsianDramas() {
        viewModelScope.launch {
            repository.getAsianDramaList(forceFetchFromRemote = false, page = 1)
                .collect { result ->
                    // Update UI state based on the result type
                    when (result) {
                        is Resource.Loading -> _uiState.update { it.copy(isLoading = result.isLoading) }
                        is Resource.Success -> _uiState.update { it.copy(dramas = result.data ?: emptyList(), isLoading = false) }
                        is Resource.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                    }
                }
        }
    }
}

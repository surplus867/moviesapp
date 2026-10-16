package com.minyu.moviesapp.details.presentation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity
import com.minyu.moviesapp.movieList.domain.model.CastMember
import com.minyu.moviesapp.movieList.domain.repository.MovieListRepository
import com.minyu.moviesapp.movieList.util.Resource
import com.minyu.moviesapp.core.LanguagePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve the movie ID from SavedStateHandle
    private val movieId = savedStateHandle.get<Int>("movieId")
    private val category = savedStateHandle.get<String>("category")

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
            movieListRepository.getMovie(id, category).collect { result ->
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
                            val isTv = movie.category.contains("drama", ignoreCase = true)
                            // Use the saved app language; fall back to the original text when TMDB has no translation.
                            val language = LanguagePrefs.get(appContext)
                                .ifBlank { Locale.getDefault().toLanguageTag() }
                            val localized = movieListRepository.getLocalizedDetails(id, isTv, language)
                            val localizedTitle = (if (isTv) localized?.name else localized?.title)
                                ?.takeIf { it.isNotBlank() }
                            val localizedOverview = localized?.overview?.takeIf { it.isNotBlank() }

                            val trailers = movieListRepository.getMovieTrailers(id, isTv)

                            val credits = movieListRepository.getMovieCredits(id, isTv)

                            val director = credits.crew
                                .firstOrNull { it.job.equals("Director", ignoreCase = true) }
                                ?.name

                            val topCast = credits.cast
                                .sortedBy { it.order ?: Int.MAX_VALUE }
                                .take(5)
                                .mapNotNull { cast ->
                                    cast.name.trim()
                                        .takeIf { it.isNotBlank() }
                                        ?.let { actor ->
                                            CastMember(
                                                name = actor,
                                                character = cast.character?.trim().orEmpty(),
                                                profilePath = cast.profilePath
                                            )
                                        }
                                }

                            // Use device region for provider lookup; repository handles fallback when unavailable.
                            val region =
                                Locale.getDefault().country.takeIf { it.isNotBlank() } ?: "US"
                            val watchProviderInfo =
                                movieListRepository.getWatchProviders(id, region, isTv)
                            // Persist both watch actions: trailer playback + where-to-watch providers.
                            _detailsState.update {
                                it.copy(
                                    movie = movie.copy(
                                        title = localizedTitle ?: movie.title,
                                        overview = localizedOverview ?: movie.overview,
                                        trailers = trailers,
                                        director = director,
                                        topCast = topCast
                                    ),
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
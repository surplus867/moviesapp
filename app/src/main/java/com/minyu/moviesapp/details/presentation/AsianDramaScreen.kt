// AsianDramaScreen.kt
// This composable displays a list of Asian dramas with images, titles, and descriptions.
// It handles loading, error, and navigation to details for each drama.
// Users can click a drama card to navigate to its details screen.

package com.minyu.moviesapp.details.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.minyu.moviesapp.R
import com.minyu.moviesapp.core.presentation.EmptyStateView
import com.minyu.moviesapp.core.presentation.ErrorStateView
import com.minyu.moviesapp.core.presentation.LoadingStateView
import com.minyu.moviesapp.core.util.ConnectivityObserver
import com.minyu.moviesapp.movieList.presentation.components.MovieItem

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AsianDramaScreen(
    navHostController: NavHostController, // Navigation controller for navigating to details
    @Suppress("UNUSED_PARAMETER")
    favoriteMoviesViewModel: FavoriteMoviesViewModel, // Reserved for future favorites actions in drama rows
    viewModel: AsianDramaViewModel = hiltViewModel() // Inject ViewModel using Hilt
) {
    // Observe the UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val offlineMessage = stringResource(R.string.no_internet_asian_movies)

    DisposableEffect(Unit) {
        onDispose { connectivityObserver.stop() }
    }

    // Show the offline message instead of a stuck spinner when nothing is cached.
    if (!isOnline && uiState.dramas.isEmpty()) {
        ErrorStateView(message = offlineMessage)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                LoadingStateView(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error != null -> {
                ErrorStateView(
                    message = stringResource(R.string.error_loading_asian_dramas),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.dramas.isEmpty() -> {
                EmptyStateView(stringResource(R.string.no_asian_dramas_found))
            }

            else -> {
                val countries = uiState.dramas.map { it.country }.distinct()
                val years = uiState.dramas
                    .mapNotNull { drama ->
                        drama.releaseDate
                            .takeIf { it.length >= 4 }
                            ?.take(4)
                            ?.takeIf { year -> year.all(Char::isDigit) }
                    }
                    .distinct()
                    .sortedDescending()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    countries.forEach { country ->
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = country,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        years.forEach { year ->
                            val dramas = uiState.dramas.filter {
                                it.country == country && it.releaseDate.startsWith(year)
                            }
                            if (dramas.isNotEmpty()) {
                                item(span = { GridItemSpan(2) }) {
                                    Text(
                                        text = year,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                items(dramas) { drama ->
                                    MovieItem(
                                        movie = drama,
                                        navHostController = navHostController,
                                        favoriteMoviesViewModel = favoriteMoviesViewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
// AsianMovieScreen.kt
// This composable displays a grid of Asian movies grouped by country and year.
// It shows a loading indicator, error message, or the movie grid depending on the state.
// Movies can be favorited and navigated to details using MovieItem.

package com.minyu.moviesapp.details.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import com.minyu.moviesapp.movieList.presentation.components.MovieItem
import com.minyu.moviesapp.core.util.ConnectivityObserver
import com.minyu.moviesapp.core.LanguagePrefs
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AsianMovieScreen(
    viewModel: AsianMovieViewModel = hiltViewModel(), // Inject ViewModel using Hilt
    navHostController: NavHostController, // Navigation controller for navigating to details
    favoriteMoviesViewModel: FavoriteMoviesViewModel, // ViewModel for managing favorites
) {
    // Fetch Asian movies when the screen is first composed
    LaunchedEffect(Unit) { viewModel.fetchAsianMovies() }
    // Observe the state from the ViewModel
    val state = viewModel.state.collectAsState()

    // Connectivity observer: show a one-time toast + message when offline instead of a stuck spinner
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val offlineToastShownState = remember { mutableStateOf(false) }

    // Determine localized offline message based on saved app language (fall back to device default)
    val savedLangTag = try { LanguagePrefs.get(context).ifBlank { Locale.getDefault().toLanguageTag() } } catch (_: Exception) { Locale.getDefault().toLanguageTag() }
    val langPrefix = savedLangTag.split("-").firstOrNull()?.lowercase(Locale.ROOT) ?: Locale.getDefault().language
    val offlineMessage = when (langPrefix) {
        "zh", "zh-cn", "zh-hk", "zh-tw" -> "無網路，無法載入亞洲電影"
        "ko" -> "인터넷에 연결되어 있지 않습니다. 아시아 영화를 불러올 수 없습니다."
        "ja" -> "インターネットに接続されていません。アジアの映画を読み込めません。"
        else -> "Unable to load asian movies, no internet connection"
    }

    // Single LaunchedEffect: reset flag when online; show the toast when offline and either loading or we don't have movies
    LaunchedEffect(isOnline, state.value.isLoading, state.value.movies.size) {
        android.util.Log.d(
            "AsianMovieScreen",
            "LaunchedEffect run: isOnline=$isOnline isLoading=${state.value.isLoading} movies=${state.value.movies.size} offlineShown=${offlineToastShownState.value}"
        )
        if (isOnline) {
            offlineToastShownState.value = false
            return@LaunchedEffect
        }

        // We want to notify only if we are loading or have no movies to display
        val shouldNotify = state.value.isLoading || state.value.movies.isEmpty()
        android.util.Log.d("AsianMovieScreen", "shouldNotify=$shouldNotify")
        if (shouldNotify && !offlineToastShownState.value) {
            android.util.Log.d("AsianMovieScreen", "showing offline toast")
            android.widget.Toast.makeText(context, offlineMessage, android.widget.Toast.LENGTH_LONG).show()
            offlineToastShownState.value = true
        }
    }

    // If offline and we have no movies, show the message immediately (covers case when not 'isLoading')
    if (!isOnline && state.value.movies.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = offlineMessage,
                color = Color.Black
            )
        }
        return
    }

    // Unregister network callback when this composable leaves
    DisposableEffect(Unit) {
        onDispose { connectivityObserver.stop() }
    }

    // Main container for the screen
    Box(modifier = Modifier.fillMaxSize()) {
        // Show loading indicator if movies are being loaded
        when {
            state.value.isLoading -> {
                if (!isOnline) {
                    Text(
                        text = offlineMessage,
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black
                    )
                } else {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            // Show error message if there is an error
            state.value.error != null -> {
                Text(
                    text = state.value.error ?: "Unknown error",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
            // Show the grid of movies grouped by country and year
            else -> {
                // Get distinct countries from the movie list
                val countries = state.value.movies.map { it.country }.distinct()
                // List of years to group movies by
                val years = listOf("2023", "2024", "2025")
                // Group movies by country and year
                val moviesByCountryAndYear = countries.associateWith { country ->
                    years.associateWith { year ->
                        state.value.movies.filter {
                            it.country == country && it.release_date.startsWith(year)
                        }
                    }
                }
                // Display movies in a vertical grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    // For each country, show a header and its movies grouped by year
                    moviesByCountryAndYear.forEach { (country, moviesByYear) ->
                        // Country header spans both columns
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = country,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        // For each year, show a header and its movies
                        moviesByYear.forEach { (year, movies) ->
                            if (movies.isNotEmpty()) {
                                // Year header spans both columns
                                item(span = { GridItemSpan(2) }) {
                                    Text(
                                        text = year,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                // Display each movie using MovieItem
                                items(movies) { movie ->
                                    MovieItem(
                                        movie = movie,
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

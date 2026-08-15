package com.minyu.moviesapp.movieList.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.minyu.moviesapp.R
import com.minyu.moviesapp.core.presentation.EmptyStateView
import com.minyu.moviesapp.core.presentation.ErrorStateView
import com.minyu.moviesapp.core.presentation.LoadingStateView
import com.minyu.moviesapp.details.presentation.FavoriteMoviesViewModel
import com.minyu.moviesapp.movieList.presentation.components.DiscoveryControls
import com.minyu.moviesapp.movieList.presentation.components.MovieItem
import com.minyu.moviesapp.movieList.presentation.components.RecommendationsSection
import com.minyu.moviesapp.movieList.util.Category
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.minyu.moviesapp.core.util.ConnectivityObserver


@Composable
fun UpcomingMoviesScreen(
    movieListState: MovieListState, // Represents the state of the upcoming movie list and its loading state
    navController: NavHostController, // Responsible for navigation between screens
    onEvent:(MovieListUiEvent) -> Unit, // Callback to handle UI events triggered in this composable
    favoriteMoviesViewModel: FavoriteMoviesViewModel
) {
    val movieKey: (com.minyu.moviesapp.movieList.domain.model.Movie) -> String = { movie ->
        val normalizedTitle = movie.title
            .ifBlank { movie.original_title }
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
        val year = movie.release_date.take(4).takeIf { it.all(Char::isDigit) }.orEmpty()
        "$normalizedTitle|$year"
    }

    // Reuse the same discovery behavior as popular tab for consistency.
    val hasDiscoveryFilters = movieListState.searchQuery.isNotBlank() ||
            movieListState.selectedYear != null ||
            movieListState.selectedLanguage != null ||
            movieListState.selectedGenre != null
    val filteredMovies = if (hasDiscoveryFilters) {
        movieListState.filteredUpcomingMovieList
    } else {
        movieListState.upcomingMovieList
    }.distinctBy(movieKey)
    val visibleMovieKeys = filteredMovies.map(movieKey).toHashSet()
    val recommendationMovies = movieListState.recommendedMovies
        .distinctBy(movieKey)
        .filterNot { movieKey(it) in visibleMovieKeys }

    // Connectivity observer (show toast + message when offline)
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val offlineToastShownState = remember { mutableStateOf(false) }

    val offlineMessage = stringResource(R.string.no_internet_upcoming_movies)
    val noResultsMessage = stringResource(R.string.no_movies_match_filters)

    // Reset the offline toast flag when we come back online so future outages will show the toast
    LaunchedEffect(isOnline) {
        if (isOnline) offlineToastShownState.value = false
    }

    // Ensure we unregister the network callback when this composable leaves
    DisposableEffect(Unit) {
        onDispose { connectivityObserver.stop() }
    }

    // Check if the upcoming movie list is empty
    if (movieListState.upcomingMovieList.isEmpty()) {
        // If offline, show a one-time toast and a user-friendly message instead of the spinner
        if (!isOnline) {
            LaunchedEffect(isOnline) {
                if (!offlineToastShownState.value) {
                    android.widget.Toast.makeText(context, offlineMessage, android.widget.Toast.LENGTH_LONG).show()
                    offlineToastShownState.value = true
                }
            }
            ErrorStateView(
                message = offlineMessage,
                modifier = Modifier.fillMaxSize()
            )
            return
        }

        // Display a loading indicator if the list is empty and we are online
        LoadingStateView()
    } else {
        // Display a LazyVerticalGrid with two columns
        LazyVerticalGrid(
            columns = GridCells.Fixed(2) ,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                // Discovery toolbar shared across list tabs.
                DiscoveryControls(
                    movieListState = movieListState,
                    onEvent = onEvent
                )
            }

            item(span = { GridItemSpan(2) }) {
                // Recommendation carousel generated from favorites + current listings.
                RecommendationsSection(
                    movies = recommendationMovies,
                    navController = navController,
                    favoriteMoviesViewModel = favoriteMoviesViewModel
                )
            }

            if (hasDiscoveryFilters && filteredMovies.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    // Inline empty state keeps the discovery controls visible for quick adjustment.
                    EmptyStateView(
                        message = noResultsMessage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            }

            // Iterate over the items in the upcoming movie list
            items(
                count = filteredMovies.size,
                key = { index -> movieKey(filteredMovies[index]) },
                contentType = { "movie" }
            ) { index ->
                // Display a MovieItem for each movie in the list
                MovieItem(
                    movie = filteredMovies[index] ,
                    navHostController = navController,
                    favoriteMoviesViewModel = favoriteMoviesViewModel
                )
                // Add vertical spacing between MovieItems
                Spacer(modifier = Modifier.height(16.dp))

                // Check if the current items is the last one and not loading
                if (index >= filteredMovies.size -1 && !movieListState.isLoading) {
                    // Paginate only when we reach the tail to avoid duplicate requests mid-list.
                    // Trigger pagination for the upcoming category
                    onEvent(MovieListUiEvent.Paginate(Category.UPCOMING))
                }

            }
        }
    }
}
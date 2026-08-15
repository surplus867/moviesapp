package com.minyu.moviesapp.movieList.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.minyu.moviesapp.R
import com.minyu.moviesapp.core.util.ConnectivityObserver
import com.minyu.moviesapp.core.presentation.EmptyStateView
import com.minyu.moviesapp.core.presentation.ErrorStateView
import com.minyu.moviesapp.core.presentation.LoadingStateView
import com.minyu.moviesapp.details.presentation.FavoriteMoviesViewModel
import com.minyu.moviesapp.movieList.presentation.components.DiscoveryControls
import com.minyu.moviesapp.movieList.presentation.components.MovieItem
import com.minyu.moviesapp.movieList.presentation.components.RecommendationsSection
import com.minyu.moviesapp.movieList.util.Category
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map


@Composable
fun PopularMoviesScreen(
    movieListState: MovieListState, // State for the movie list and loading
    navController: NavHostController, // For navigation
    onEvent: (MovieListUiEvent) -> Unit, // Handles UI events (like pagination)
    favoriteMoviesViewModel: FavoriteMoviesViewModel // For favorite movies
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

    val gridState = rememberLazyGridState() // Tracks grid scroll state
    // Apply discovery filters only when at least one control is active.
    val hasDiscoveryFilters = movieListState.searchQuery.isNotBlank() ||
            movieListState.selectedYear != null ||
            movieListState.selectedLanguage != null ||
            movieListState.selectedGenre != null
    val filteredMovies = if (hasDiscoveryFilters) {
        movieListState.filteredPopularMovieList
    } else {
        movieListState.popularMovieList
    }.distinctBy(movieKey)
    val visibleMovieKeys = filteredMovies.map(movieKey).toHashSet()
    val recommendationMovies = movieListState.recommendedMovies
        .distinctBy(movieKey)
        .filterNot { movieKey(it) in visibleMovieKeys }

    // Connectivity observer (show toast + message when offline)
    val context = LocalContext.current
    val connectivityObserver = androidx.compose.runtime.remember { ConnectivityObserver(context) }
    val isOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val offlineToastShownState = remember { mutableStateOf(false) }
    val offlineMessage = stringResource(R.string.no_internet_movies)
    val noResultsMessage = stringResource(R.string.no_movies_match_filters)

    // Reset the offline toast flag when we come back online so future outages will show the toast
    LaunchedEffect(isOnline) {
        if (isOnline) {
            offlineToastShownState.value = false
        }
    }

    // Ensure we unregister network callback when this composable leaves
    DisposableEffect(Unit) {
        onDispose {
            connectivityObserver.stop()
        }
    }

    // If no movies, show loading spinner or offline message
    if (movieListState.popularMovieList.isEmpty()) {
        if (!isOnline) {
            LaunchedEffect(isOnline) {
                if (!offlineToastShownState.value) {
                    android.widget.Toast.makeText(context, offlineMessage, android.widget.Toast.LENGTH_LONG).show()
                    offlineToastShownState.value = true
                }
            }
            ErrorStateView(message = offlineMessage)
            return
        }

        // First load path: no cached data yet, so show full-screen loading state.
        LoadingStateView()
        return
    }

    // Pagination: load more when near the end
    LaunchedEffect(gridState, filteredMovies, movieListState.isLoading) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = gridState.layoutInfo.totalItemsCount
            lastVisible to total
        }
            // Trigger when we are close to the end to hide network latency during scroll.
            .map { (lastVisible, total) -> lastVisible >= total - 5 && total > 0 }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore && !movieListState.isLoading) {
                    onEvent(MovieListUiEvent.Paginate(Category.POPULAR))
                }
            }
    }

    // Display movies in a grid
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            // Discovery toolbar: search + year/language/genre filters.
            DiscoveryControls(
                movieListState = movieListState,
                onEvent = onEvent
            )
        }

        item(span = { GridItemSpan(2) }) {
            // Recommendation carousel generated from current catalog + favorites profile.
            RecommendationsSection(
                movies = recommendationMovies,
                navController = navController,
                favoriteMoviesViewModel = favoriteMoviesViewModel
            )
        }

        if (hasDiscoveryFilters && filteredMovies.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                // Inline empty state: list has data overall, but current filter combination has no matches.
                EmptyStateView(
                    message = noResultsMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }

        items(
            filteredMovies.size,
            key = { index -> movieKey(filteredMovies[index]) },
            contentType = { "movie" }
        ) { index ->
            val movie = filteredMovies[index]
            MovieItem(
                movie = movie,
                navHostController = navController,
                favoriteMoviesViewModel = favoriteMoviesViewModel
            )
            // Show loading spinner at the bottom if loading more
            if (movieListState.isLoading && movieListState.popularMovieList.isNotEmpty()) {
                LoadingStateView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }
}
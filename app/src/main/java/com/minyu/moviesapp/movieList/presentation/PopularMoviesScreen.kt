package com.minyu.moviesapp.movieList.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.minyu.moviesapp.core.util.ConnectivityObserver
import com.minyu.moviesapp.details.presentation.FavoriteMoviesViewModel
import com.minyu.moviesapp.movieList.presentation.components.MovieItem
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
    val gridState = rememberLazyGridState() // Tracks grid scroll state

    // Connectivity observer (show toast + message when offline)
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val offlineToastShownState = remember { mutableStateOf(false) }

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
                    android.widget.Toast.makeText(context, "No internet, unable to load movies", android.widget.Toast.LENGTH_LONG).show()
                    offlineToastShownState.value = true
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No internet, unable to load movies")
            }
            return
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Pagination: load more when near the end
    LaunchedEffect(gridState, movieListState.popularMovieList, movieListState.isLoading) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = gridState.layoutInfo.totalItemsCount
            lastVisible to total
        }
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
        items(
            movieListState.popularMovieList.size,
            key = { index -> movieListState.popularMovieList[index].id },
            contentType = { "movie" }
        ) { index ->
            val movie = movieListState.popularMovieList[index]
            MovieItem(
                movie = movie,
                navHostController = navController,
                favoriteMoviesViewModel = favoriteMoviesViewModel
            )
            // Show loading spinner at the bottom if loading more
            if (movieListState.isLoading && movieListState.popularMovieList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
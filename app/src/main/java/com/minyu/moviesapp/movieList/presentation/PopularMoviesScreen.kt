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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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
    val scope = rememberCoroutineScope() // For launching coroutines (not used here)

    // If no movies, show loading spinner
    if (movieListState.popularMovieList.isEmpty()) {
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
                movie = movieListState.popularMovieList[index],
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
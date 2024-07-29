package com.minyu.moviesapp.movieList.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.minyu.moviesapp.movieList.presentation.components.MovieItem
import com.minyu.moviesapp.movieList.util.Category


@Composable
fun PopularMoviesScreen(
    movieListState: MovieListState, // State representing the popular movie list ad its loading state
    navController: NavHostController, // Navigation controller for navigating to other screens
    onEvent:(MovieListUiEvent) -> Unit // Callback to handle UI events
) {
    // Check if the popular movie list is empty
    if (movieListState.popularMovieList.isEmpty()) {
        // Display a loading indicator if the list is empty
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Display a LazyVerticalGrid with two columns
        LazyVerticalGrid(
            columns = GridCells.Fixed(2) ,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
        ) {
            // Iterate over the items in the popular movie list
            items(movieListState.popularMovieList.size) { index ->
                // Display a MovieItem for each movie in the list
                MovieItem(
                    movie = movieListState.popularMovieList[index] ,
                    navHostController = navController
                )
                // Add vertical spacing between MovieItems
                Spacer(modifier = Modifier.height(16.dp))

                // Check if the current item is the last one and not loading
                if (index >= movieListState.popularMovieList.size -1 && !movieListState.isLoading) {
                    // Trigger pagination for the popular category
                    onEvent(MovieListUiEvent.Paginate(Category.POPULAR))
                }

            }
        }
    }
}
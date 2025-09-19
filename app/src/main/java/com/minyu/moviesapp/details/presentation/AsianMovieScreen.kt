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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.minyu.moviesapp.movieList.presentation.components.MovieItem

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

    // Main container for the screen
    Box(modifier = Modifier.fillMaxSize()) {
        // Show loading indicator if movies are being loaded
        when {
            state.value.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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

package com.minyu.moviesapp.details.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.layout.padding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.minyu.moviesapp.movieList.presentation.components.MovieItem

@Composable
fun AsianMovieScreen(
    viewModel: AsianMovieViewModel = hiltViewModel(),
    navHostController: NavHostController,
    favoriteMoviesViewModel: FavoriteMoviesViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.fetchAsianMovies()
    }

    val state = viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.value.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.value.error != null -> {
                Text(
                    text = state.value.error ?: "Unknown error",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                val countries = state.value.movies.map { it.country }.distinct()
                val years = listOf("2023", "2024", "2025")
                val moviesByCountryAndYear = countries.associateWith { country ->
                    years.associateWith { year ->
                        state.value.movies.filter {
                            it.country == country && it.release_date.startsWith(year)
                        }
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    moviesByCountryAndYear.forEach { (country, moviesByYear) ->
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = country,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        moviesByYear.forEach { (year, movies) ->
                            if (movies.isNotEmpty()) {
                                item(span = { GridItemSpan(2) }) {
                                    Text(
                                        text = year,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
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
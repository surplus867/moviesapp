package com.minyu.moviesapp.details.presentation


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.minyu.moviesapp.movieList.presentation.components.FavoriteMovieItem


@Composable
fun FavoriteMoviesScreen(viewModel: FavoriteMoviesViewModel) {
    val favoriteMovies by viewModel.favoriteMovies.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(favoriteMovies) { movie ->
            FavoriteMovieItem(
                movie = movie,
                onRemove = { viewModel.removeFavorite(it.id) }
            )
        }
    }
}
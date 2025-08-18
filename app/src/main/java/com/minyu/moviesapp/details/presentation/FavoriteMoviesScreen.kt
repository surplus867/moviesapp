package com.minyu.moviesapp.details.presentation


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.minyu.moviesapp.movieList.presentation.components.FavoriteMovieItem

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FavoriteMoviesScreen(
    viewModel: FavoriteMoviesViewModel,
    navController: NavHostController
) {
    val favoriteMovies by viewModel.favoriteMovies.collectAsState()


    androidx.compose.material3.Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Movies") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(8.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favoriteMovies) { movie ->
                FavoriteMovieItem(
                    movie = movie,
                    onClick = { navController.navigate("details/${movie.id}") },
                    onRemove = { viewModel.removeFavorite(it.id) }
                )
            }
        }
    }
}
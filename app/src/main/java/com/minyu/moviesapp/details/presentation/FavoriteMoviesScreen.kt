package com.minyu.moviesapp.details.presentation


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.minyu.moviesapp.R
import com.minyu.moviesapp.core.presentation.EmptyStateView
import com.minyu.moviesapp.movieList.presentation.components.FavoriteMovieItem


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FavoriteMoviesScreen(
    viewModel: FavoriteMoviesViewModel,
    navController: NavHostController
) {
    // Collect favorite movies from ViewModel as state
    val favoriteMovies by viewModel.favoriteMovies.collectAsState()
    // State for search query and sort option
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("Title") }
    val titleSort = stringResource(R.string.sort_title)
    val dateAddedSort = stringResource(R.string.sort_date_added)
    // Keep sort values localized, because the selected label is shown directly in the button text.

    // Filter and sort movies based on search and sort option
    val filteredMovies = favoriteMovies
        .filter { it.title.contains(searchQuery, ignoreCase = true) }
        .let {
            when (sortOption) {
                titleSort -> it.sortedBy { movie -> movie.title }
                dateAddedSort -> it.sortedByDescending { movie ->  movie.dateAdded }
                else -> it
            }
        }

    Scaffold(
        topBar = {
            // App bar with title and back button
            TopAppBar(
                title = { Text(stringResource(R.string.favorite_movies_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                .padding(innerPadding)
        ) {
            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.search_hint)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                trailingIcon = {
                    // Clear search button
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.clear_search)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Sort dropdown menu
            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expanded = true }) {
                    Text(stringResource(R.string.sort_prefix, sortOption))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(titleSort) },
                        onClick = {
                            sortOption = titleSort
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(dateAddedSort) },
                        onClick = {
                            sortOption = dateAddedSort
                            expanded = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Show empty state or list of favorite movies
            if (filteredMovies.isEmpty()) {
                // Empty view covers both "no favorites yet" and "no search match" in one localized message.
                EmptyStateView(
                    message = stringResource(R.string.no_favorite_movies_found),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Render each favorite movie item
                    items(filteredMovies) { movie ->
                        FavoriteMovieItem(
                            movie = movie,
                            onClick = { navController.navigate("details/${movie.id}") },
                            onRemove = { viewModel.removeFavorite(it.id) }
                        )
                    }
                }
            }
        }
    }
}
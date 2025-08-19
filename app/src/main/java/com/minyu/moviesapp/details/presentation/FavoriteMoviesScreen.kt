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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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

    // Filter and sort movies based on search and sort option
    val filteredMovies = favoriteMovies
        .filter { it.title.contains(searchQuery, ignoreCase = true) }
        .let {
            when (sortOption) {
                "Title" -> it.sortedBy { movie -> movie.title }
                "Date Added" -> it.sortedByDescending { movie ->  movie.dateAdded }
                else -> it
            }
        }

    Scaffold(
        topBar = {
            // App bar with title and back button
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
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                trailingIcon = {
                    // Clear search button
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Clear Search"
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
                    Text("Sort: $sortOption")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Title") },
                        onClick = {
                            sortOption = "Title"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Date Added") },
                        onClick = {
                            sortOption = "Date Added"
                            expanded = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Show empty state or list of favorite movies
            if (filteredMovies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Text("No favorite movies found.")
                }
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
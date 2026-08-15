// AsianDramaScreen.kt
// This composable displays a list of Asian dramas with images, titles, and descriptions.
// It handles loading, error, and navigation to details for each drama.
// Users can click a drama card to navigate to its details screen.

package com.minyu.moviesapp.details.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.minyu.moviesapp.R
import com.minyu.moviesapp.core.presentation.EmptyStateView
import com.minyu.moviesapp.core.presentation.ErrorStateView
import com.minyu.moviesapp.core.presentation.LoadingStateView

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AsianDramaScreen(
    navHostController: NavHostController, // Navigation controller for navigating to details
    @Suppress("UNUSED_PARAMETER")
    favoriteMoviesViewModel: FavoriteMoviesViewModel, // Reserved for future favorites actions in drama rows
    viewModel: AsianDramaViewModel = hiltViewModel() // Inject ViewModel using Hilt
) {
    // Observe the UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Scaffold provides the top app bar and main content area
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.asian_drama)) })
        }
    ) { padding ->
        // Main content area with padding from Scaffold
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Show loading indicator if dramas are being loaded
            when {
                uiState.isLoading -> {
                    LoadingStateView()
                }
                // Show error message if there is an error (use localized string)
                uiState.error != null -> {
                    // Route all failure copy through resources for localization consistency.
                    ErrorStateView(stringResource(R.string.error_loading_asian_dramas))
                }
                uiState.dramas.isEmpty() -> {
                    // Dedicated empty state makes "loaded but empty" clear vs loading/error.
                    EmptyStateView(stringResource(R.string.no_asian_dramas_found))
                }
                // Show the list of dramas if data is loaded
                else -> {
                    LazyColumn {
                        items(uiState.dramas) { drama ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        navHostController.navigate("details/${drama.id}")
                                    },
                                elevation = CardDefaults.cardElevation()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Poster image for the drama
                                    AsyncImage(
                                        model = drama.poster_path,
                                        contentDescription = drama.title,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(end = 8.dp)
                                    )
                                    // Column for title and overview
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = drama.title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = drama.overview,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )
                        }
                    }
                }
            }
        }
    }
}
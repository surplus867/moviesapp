package com.minyu.moviesapp.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Upcoming
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.minyu.moviesapp.R
import com.minyu.moviesapp.details.presentation.FavoriteMoviesViewModel
import com.minyu.moviesapp.details.presentation.AsianMovieScreen
import com.minyu.moviesapp.movieList.presentation.MovieListUiEvent
import com.minyu.moviesapp.movieList.presentation.MovieListViewModel
import com.minyu.moviesapp.movieList.presentation.PopularMoviesScreen
import com.minyu.moviesapp.movieList.presentation.UpcomingMoviesScreen
import com.minyu.moviesapp.movieList.util.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    // Get the ViewModel for the movie list and favorites using Hilt
    val movieListViewModel = hiltViewModel<MovieListViewModel>()
    val favoriteMoviesViewModel = hiltViewModel<FavoriteMoviesViewModel>()

    // Observe the current state of the movie list
    val movieListState = movieListViewModel.movieListState.collectAsState().value

    // Create a NavController for the bottom navigation bar
    val bottomNavController = rememberNavController()

    // Scaffold provides the basic layout structure with top and bottom bars
    Scaffold(
        bottomBar = {
            // Custom bottom navigation bar for switching between screens
            BottomNavigationBar(

                bottomNavController = bottomNavController,
                onEvent = movieListViewModel::onEvent
            )
        },
        topBar = {
            // Top app bar with dynamic title based on the current screen
            TopAppBar(
                title = {
                    Text(
                        text = if (movieListState.isCurrentPopularScreen)
                            stringResource(R.string.popular_movies)
                        else
                            stringResource(R.string.upcoming_movies),
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("language_screen_route") {
                            popUpTo("language_screen_route") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.shadow(2.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    MaterialTheme.colorScheme.inverseOnSurface
                )
            )
        }
    ) {
        // Main content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // Navigation host for switching between popular and upcoming movie screens
            NavHost(
                navController = bottomNavController,
                startDestination = Screen.PopularMovieList.route
            ) {
                composable(Screen.PopularMovieList.route) {
                    PopularMoviesScreen(
                        navController = navController,
                        movieListState = movieListState,
                        onEvent = movieListViewModel::onEvent,
                        favoriteMoviesViewModel = favoriteMoviesViewModel
                    )
                }
                composable(Screen.UpcomingMovieList.route) {
                    UpcomingMoviesScreen(
                        navController = navController,
                        movieListState = movieListState,
                        onEvent = movieListViewModel::onEvent,
                        favoriteMoviesViewModel = favoriteMoviesViewModel
                    )
                }
                composable(Screen.AsianMovieList.route) {
                    // Call your Korean Screen/route here
                    // If you already have a VM + Screen, use that:
                    AsianMovieScreen(
                        navHostController = navController,
                        favoriteMoviesViewModel = favoriteMoviesViewModel
                    )
                }
            }
        }
    }
}

// Bottom navigation bar composable for switching between movie lists
@Composable
fun BottomNavigationBar(
    bottomNavController: NavHostController,
    onEvent: (MovieListUiEvent) -> Unit
) {
    // Define the items for the bottom navigation bar
    val items = listOf(
        BottomItem(title = stringResource(R.string.popular), icon = Icons.Rounded.Movie),
        BottomItem(title = stringResource(R.string.upcoming), icon = Icons.Rounded.Upcoming),
        BottomItem(title = stringResource(R.string.asian), icon = Icons.Rounded.Upcoming)
    )

    // Remember the currently selected index
    val selected = rememberSaveable { mutableIntStateOf(0) }

    // NavigationBar provides the Material3 bottom navigation UI
    NavigationBar {
        Row(
            modifier = Modifier.background(MaterialTheme.colorScheme.inverseOnSurface)
        ) {
            items.forEachIndexed { index, bottomItem ->
                NavigationBarItem(
                    selected = selected.intValue == index,
                    onClick = {
                        selected.intValue = index
                        onEvent(MovieListUiEvent.Navigate)
                        // Handle navigation and event when an item is selected

                        val route = when (index) {
                            0 -> Screen.PopularMovieList.route
                            1 -> Screen.UpcomingMovieList.route
                            else -> Screen.AsianMovieList.route
                        }
                        bottomNavController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(bottomNavController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = bottomItem.icon,
                            contentDescription = bottomItem.title,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    label = {
                        Text(
                            text = bottomItem.title,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
            }
        }
    }
}

data class BottomItem(
    val title: String,
    val icon: ImageVector
)
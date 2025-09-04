package com.minyu.moviesapp.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.minyu.moviesapp.core.presentation.HomeScreen
import com.minyu.moviesapp.details.presentation.AsianMovieViewModel
import com.minyu.moviesapp.details.presentation.DetailsScreen
import com.minyu.moviesapp.details.presentation.FavoriteMoviesScreen
import com.minyu.moviesapp.details.presentation.FavoriteMoviesViewModel
import com.minyu.moviesapp.details.presentation.AsianMovieScreen
import com.minyu.moviesapp.movieList.util.Screen
import com.minyu.moviesapp.ui.theme.MoviesappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply the app's theme
            MoviesappTheme {
                // Set the system bar color
                SetBarColor(color = MaterialTheme.colorScheme.inverseOnSurface)
                // Main surface container with background color
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create a navigation controller
                    val navController = rememberNavController()

                    // Set up navigation host with routes
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        // Home screen route
                        composable(Screen.Home.route) {
                            HomeScreen(navController)
                        }
                        // Details screen route with movieId argument
                        composable(
                            Screen.Details.route + "/{movieId}",
                            arguments = listOf(
                                navArgument("movieId") { type = NavType.IntType }
                            )
                        ) {
                            DetailsScreen(navController)
                        }
                        // Favorite movies screen route
                        composable("favorite_movies") {
                            // Obtain ViewModel using Hilt
                            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<FavoriteMoviesViewModel>()
                            FavoriteMoviesScreen(viewModel, navController)
                        }

                        // Asian movies screen route
                        composable("asian_movies") {
                            val favoriteMoviesViewModel = androidx.hilt.navigation.compose.hiltViewModel<FavoriteMoviesViewModel>()
                            val asianMovieViewModel = androidx.hilt.navigation.compose.hiltViewModel<AsianMovieViewModel>()
                            AsianMovieScreen(
                                navHostController = navController,
                                favoriteMoviesViewModel = favoriteMoviesViewModel,
                                viewModel = asianMovieViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    // Helper composable to set system bar color
    @Composable
    private fun SetBarColor(color: Color) {
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(key1 = color) {
            systemUiController.setSystemBarsColor(color)
        }
    }
}
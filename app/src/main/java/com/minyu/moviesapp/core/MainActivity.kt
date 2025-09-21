package com.minyu.moviesapp.core

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.minyu.moviesapp.core.presentation.HomeScreen
import com.minyu.moviesapp.core.presentation.LanguageSelectionScreen
import com.minyu.moviesapp.details.presentation.AsianDramaScreen
import com.minyu.moviesapp.details.presentation.AsianMovieScreen
import com.minyu.moviesapp.details.presentation.AsianMovieViewModel
import com.minyu.moviesapp.details.presentation.DetailsScreen
import com.minyu.moviesapp.details.presentation.FavoriteMoviesScreen
import com.minyu.moviesapp.details.presentation.FavoriteMoviesViewModel
import com.minyu.moviesapp.movieList.util.Screen
import com.minyu.moviesapp.ui.theme.MoviesappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Attach locale to context for language support
    override fun attachBaseContext(newBase: Context) {
        val saved = LanguagePrefs.get(newBase) // "en", "ko", "ja", "zh-HK"
        val wrapped = if (saved.isNotBlank()) LocaleHelper.wrapWithLocale(newBase, saved) else newBase
        super.attachBaseContext(wrapped)
    }

    // Main entry point: sets up edge-to-edge, theme, and navigation
    override fun onCreate(savedInstanceState: Bundle?) {
        // Set app locale for all components
        val saved = LanguagePrefs.get(this)
        if (saved.isNotBlank()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(saved))
        }

        // Enable edge-to-edge display for all Android versions
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            MoviesappTheme {
                // Set system bar color to transparent for edge-to-edge
                SetBarColor(color = Color.Transparent)
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        // Handle system bar insets so content is not obscured
                        .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.systemBars),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    // Navigation graph for all screens
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        // Home screen
                        composable(Screen.Home.route) {
                            HomeScreen(navController)
                        }
                        // Language selection screen
                        composable("Language_screen_route") {
                            LanguageSelectionScreen { selectedLanguage ->
                                LanguagePrefs.set(this@MainActivity, selectedLanguage)
                                navController.popBackStack()
                                recreate()
                            }
                        }
                        // Movie details screen
                        composable(
                            Screen.Details.route + "/{movieId}",
                            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
                        ) {
                            DetailsScreen(navController)
                        }
                        // Favorite movies screen
                        composable("favorite_movies") {
                            val vm = hiltViewModel<FavoriteMoviesViewModel>()
                            FavoriteMoviesScreen(vm, navController)
                        }
                        // Asian movies screen
                        composable("asian_movies") {
                            val favVm = hiltViewModel<FavoriteMoviesViewModel>()
                            val asianVm = hiltViewModel<AsianMovieViewModel>()
                            AsianMovieScreen(
                                navHostController = navController,
                                favoriteMoviesViewModel = favVm,
                                viewModel = asianVm
                            )
                        }
                        // Asian drama screen
                        composable(Screen.AsianDramaList.route) {
                            val favVm = hiltViewModel<FavoriteMoviesViewModel>()
                            AsianDramaScreen(
                                navHostController = navController,
                                favoriteMoviesViewModel = favVm
                            )
                        }
                    }
                }
            }
        }
    }

    // Sets system bar color using accompanist SystemUiController
    @Composable
    private fun SetBarColor(color: Color) {
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(color) {
            systemUiController.setSystemBarsColor(color = color, darkIcons = true)
        }
    }
}

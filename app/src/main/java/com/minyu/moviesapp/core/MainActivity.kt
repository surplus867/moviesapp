package com.minyu.moviesapp.core

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.minyu.moviesapp.core.presentation.HomeScreen
import com.minyu.moviesapp.core.presentation.LanguageSelectionScreen
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

    // Ensure this Activity is born with the saved locale
    override fun attachBaseContext(newBase: Context) {
        val saved = LanguagePrefs.get(newBase) // "en", "ko", "ja", "zh-HK"
        val wrapped = if (saved.isNotBlank()) LocaleHelper.wrapWithLocale(newBase, saved) else newBase
        super.attachBaseContext(wrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Also apply per-app locales (AppCompat) to persist + help other components
        val saved = LanguagePrefs.get(this)
        if (saved.isNotBlank()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(saved))
        }

        super.onCreate(savedInstanceState)
        setContent {
            MoviesappTheme {
                SetBarColor(color = MaterialTheme.colorScheme.inverseOnSurface)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(navController)
                        }
                        composable("Language_screen_route") {
                            LanguageSelectionScreen { selectedLanguage ->
                                LanguagePrefs.set(this@MainActivity, selectedLanguage)
                                navController.popBackStack()
                                recreate()
                            }
                        }
                        composable(
                            Screen.Details.route + "/{movieId}",
                            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
                        ) {
                            DetailsScreen(navController)
                        }
                        composable("favorite_movies") {
                            val vm = androidx.hilt.navigation.compose.hiltViewModel<FavoriteMoviesViewModel>()
                            FavoriteMoviesScreen(vm, navController)
                        }
                        composable("asian_movies") {
                            val favVm = androidx.hilt.navigation.compose.hiltViewModel<FavoriteMoviesViewModel>()
                            val asianVm = androidx.hilt.navigation.compose.hiltViewModel<AsianMovieViewModel>()
                            AsianMovieScreen(
                                navHostController = navController,
                                favoriteMoviesViewModel = favVm,
                                viewModel = asianVm
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SetBarColor(color: Color) {
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(color) { systemUiController.setSystemBarsColor(color) }
    }
}

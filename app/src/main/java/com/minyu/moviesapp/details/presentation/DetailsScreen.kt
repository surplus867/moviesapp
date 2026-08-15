package com.minyu.moviesapp.details.presentation

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.minyu.moviesapp.R
import com.minyu.moviesapp.core.auth.SessionManager
import android.content.pm.ApplicationInfo
import com.minyu.moviesapp.core.util.ConnectivityObserver
import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity
import com.minyu.moviesapp.movieList.data.remote.MovieApi
import com.minyu.moviesapp.movieList.data.remote.respond.TrailerDto
import com.minyu.moviesapp.movieList.domain.model.WatchProviderInfo
import com.minyu.moviesapp.movieList.util.RatingBar
import kotlinx.coroutines.launch
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.net.toUri

// Helper function to format release date nicely
fun formatReleaseDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(dateString)
        if (date != null) {
            val cal = java.util.Calendar.getInstance()
            cal.time = date
            val month = SimpleDateFormat("MMM", Locale.US).format(date)
            val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val year = cal.get(java.util.Calendar.YEAR)
            "$month $day $year"
        } else dateString
    } catch (e: Exception) {
        dateString
    }
}

// Main details screen composable
@Composable
fun DetailsScreen(navController: NavController, selectedLang: String = "zh") {
    // Get ViewModel and state
    val detailsViewModel = hiltViewModel<DetailsViewModel>()
    val translateViewModel = hiltViewModel<DetailsTranslateViewModel>()
    val detailsState = detailsViewModel.detailsState.collectAsState().value
    val reviews by detailsViewModel.reviews.collectAsState(emptyList<MovieReviewEntity>())

    // Connectivity observer: show an offline banner when there is no internet
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isOnline by connectivityObserver.isOnline.collectAsState(initial = true)

    // Remember whether we've already shown the offline toast for the current offline period
    var offlineToastShown by remember { mutableStateOf(false) }

    // Show a short Toast once when connectivity is lost, reset flag when back online
    LaunchedEffect(isOnline) {
        if (!isOnline && !offlineToastShown) {
            android.widget.Toast.makeText(context, "No internet connection", android.widget.Toast.LENGTH_SHORT).show()
            offlineToastShown = true
        } else if (isOnline) {
            // reset when connectivity is restored so future outages will show a toast again
            offlineToastShown = false
        }
    }

    // Ensure we unregister the network callback when this composable leaves
    DisposableEffect(Unit) {
        onDispose {
            connectivityObserver.stop()
        }
    }

    val translated by translateViewModel.translatedPlot.collectAsState(initial = null)
    val original = detailsState.movie?.overview ?: ""

    // normalize selected/target language for translator
    val normalizedTargetLang = when (selectedLang.lowercase(Locale.ROOT)) {
        "zh" -> "zh-CN"
        else -> selectedLang
    }
    // short code to send to translator (many translator APIS expect the 2-letter code)
    val translatorTargetLang =
        normalizedTargetLang.split("-").firstOrNull().orEmpty().lowercase(Locale.ROOT)

    // Trigger translation when overview or selected language changes
    LaunchedEffect(
        detailsState.movie?.id,
        normalizedTargetLang,
        detailsState.movie?.original_language,
        isOnline // also react to connectivity changes
    ) {
        val movie = detailsState.movie ?: return@LaunchedEffect
        val movieOverview = movie.overview.orEmpty()
        val movieOriginLang = movie.original_language.orEmpty()
        val moviePrefix = movieOriginLang.split("-").firstOrNull().orEmpty().lowercase(Locale.ROOT)
        val targetPrefix =
            normalizedTargetLang.split("-").firstOrNull().orEmpty().lowercase(Locale.ROOT)

        val sameLang = movieOriginLang.equals(normalizedTargetLang, ignoreCase = true) ||
                moviePrefix == targetPrefix

        // debug log to verify trigger conditions
        android.util.Log.d(
            "DetailsScreen",
            "translate trigger overviewPresent=${movieOverview.isNotBlank()} sameLang=$sameLang origin=$movieOriginLang target=$translatorTargetLang isOnline=$isOnline"
        )

        // If offline, don't attempt translation
        if (!isOnline) return@LaunchedEffect

        if (movieOverview.isNotBlank() && !sameLang) {
            // use the normalized full target (e.g. "zh-CN") and the full origin if available
            val sourceForApi = movieOriginLang.takeIf { it.isNotBlank() } ?: moviePrefix
            val targetForApi = normalizedTargetLang

            android.util.Log.d(
                "DetailsScreen",
                "calling translateViewModel.translatePlot len=${movieOverview.length} source=$sourceForApi target=$targetForApi"
            )

            translateViewModel.translatePlot(
                movieOverview,
                targetLang = targetForApi,
                sourceLang = sourceForApi
            )
        }
    }

    // Load images using Coil for backdrop and poster only when online; otherwise show placeholders
    val backDropImageState = if (isOnline) {
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(MovieApi.IMAGE_BASE_URL + detailsState.movie?.backdrop_path)
                .size(Size.ORIGINAL)
                .build()
        ).state
    } else {
        null
    }

    val posterImageState = if (isOnline) {
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(MovieApi.IMAGE_BASE_URL + detailsState.movie?.poster_path)
                .size(Size.ORIGINAL)
                .build()
        ).state
    } else {
        null
    }

    var reviewText by remember { mutableStateOf("") }

    // Main vertical layout with scroll
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            // Placeholder for alignment
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Title and release date
        Text(
            text = detailsState.movie?.title ?: "",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = formatReleaseDate(detailsState.movie?.release_date),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Backdrop image with play button overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            val trailerKey = remember(detailsState.movie?.trailers) {
                selectPreferredTrailerKey(detailsState.movie?.trailers)
            }
            val backdropPainter = if (backDropImageState is AsyncImagePainter.State.Success) {
                backDropImageState.painter
            } else {
                painterResource(id = R.drawable.movie_logo)
            }

            Image(
                painter = backdropPainter,
                contentDescription = "Backdrop image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (trailerKey.isNotBlank()) {
                IconButton(
                    onClick = {
                        android.util.Log.d("DetailsScreen", "Opening trailer in YouTube, trailerKey=$trailerKey")
                        val opened = openTrailerInYouTube(context, trailerKey)
                        if (!opened) {
                            android.widget.Toast.makeText(
                                context,
                                "Trailer link is unavailable right now.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(32.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Watch trailer on YouTube",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Watch trailer on YouTube",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Rating and language
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            // Rating
            RatingBar(
                starsModifier = Modifier.size(18.dp),
                rating = (detailsState.movie?.vote_average ?: 0.0) / 2.0
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Language
            Text(
                text = detailsState.movie?.original_language?.uppercase() ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        // Overview section
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Translated overview text (if available) or original overview as fallback
        val overviewText = translated ?: original
        Text(
            text = overviewText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Show streaming options only when provider metadata exists for a region.
        detailsState.watchProviderInfo?.let { watchProviderInfo ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Where to Watch (${watchProviderInfo.region})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val providerSummary = if (watchProviderInfo.providers.isNotEmpty()) {
                watchProviderInfo.providers.joinToString(", ")
            } else {
                "Available providers can be viewed via the link below."
            }

            Text(
                text = providerSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (watchProviderInfo.link.isNotBlank()) {
                Button(
                    onClick = {
                        val opened = openWatchProviderLink(context, watchProviderInfo)
                        if (!opened) {
                            android.widget.Toast.makeText(
                                context,
                                "Unable to open watch provider link.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Open streaming options")
                }
            }
        }

        // Review section
        Text(
            text = "Reviews",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (reviews.isNotEmpty()) {
            // Show list of reviews
            for (review in reviews) {
                val author = review.userName ?: "Unknown"
                val content = review.comment ?: ""

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Review author and date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = author,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "• ${review.timestamp.takeIf { it > 0 }?.let { formatReleaseDate(it.toString()) } ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Review content
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            // No reviews yet
            Text(
                text = "No reviews yet. Be the first to review!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        // Add review section (visible only to authenticated users)
        // Simple local auth: check SessionManager
        val scope = rememberCoroutineScope()
        val loggedIn = remember { mutableStateOf(SessionManager.isLoggedIn(context)) }
        val userName = remember { mutableStateOf(SessionManager.getUserName(context) ?: "") }

        if (loggedIn.value) {
            // Show add review form
            var rating by remember { mutableStateOf(0.0) }
            var reviewContent by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Write a Review",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Rating bar
                RatingBar(
                    starsModifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    rating = rating
                )

                // Review text field
                var localReview by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = localReview,
                    onValueChange = { localReview = it },
                    label = { Text("Your Review") },
                    placeholder = { Text("What's your opinion?") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit button
                Button(
                    onClick = {
                        detailsState.movie?.let { movie ->
                            detailsViewModel.insertReview(
                                MovieReviewEntity(
                                    movieId = movie.id,
                                    userName = userName.value.ifBlank { "Anonymous" },
                                    rating = rating.toFloat(),
                                    comment = localReview,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                        // clear
                        localReview = ""
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = localReview.isNotBlank()
                ) {
                    Text("Submit Review")
                }
            }
        } else {
            // Inline login form (very simple local example)
            // Prefill demo credentials in debug builds to speed up testing
            val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val defaultUser = if (isDebuggable) "demo_user" else ""
            val defaultPass = if (isDebuggable) "demo_pass" else ""
            var nameField by remember { mutableStateOf(defaultUser) }
            var passField by remember { mutableStateOf(defaultPass) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text("Log in to add a review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = nameField, onValueChange = { nameField = it }, placeholder = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = passField, onValueChange = { passField = it }, placeholder = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
                    Button(onClick = {
                        // very simple auth: accept any non-empty username
                        if (nameField.isNotBlank()) {
                            scope.launch {
                                SessionManager.setLoggedIn(context, true, nameField.trim())
                                userName.value = nameField.trim()
                                loggedIn.value = true
                            }
                        }
                    }, enabled = nameField.isNotBlank()) { Text("Log in") }
                }
            }
        }

        // Spacer to push content above navigation bar (use padding with WindowInsets)
        Spacer(modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()))
    }
}

// Helper to resolve a likely YouTube video id from a key or URL
private fun resolveYouTubeId(keyOrUrl: String): String {
    if (keyOrUrl.isBlank()) return ""
    // If it's a URL, try to extract v= or last path segment
    return try {
        val uri = keyOrUrl.toUri()
        val v = uri.getQueryParameter("v")
        if (!v.isNullOrBlank()) return v
        // handle youtube short urls and /embed/ segments
        val path = uri.path ?: ""
        val last = path.split('/').lastOrNull().orEmpty()
        last.ifBlank { keyOrUrl }
    } catch (_: Exception) {
        keyOrUrl
    }
}

private fun selectPreferredTrailerKey(trailers: List<TrailerDto>?): String {
    // Prioritize official YouTube trailers, then relax constraints as fallback.
    return trailers
        ?.firstOrNull {
            it.site.equals("YouTube", ignoreCase = true) &&
                    it.type.equals("Trailer", ignoreCase = true) &&
                    it.official == true &&
                    !it.key.isNullOrBlank()
        }
        ?.key
        ?: trailers
            ?.firstOrNull {
                it.site.equals("YouTube", ignoreCase = true) &&
                        it.type.equals("Trailer", ignoreCase = true) &&
                        !it.key.isNullOrBlank()
            }
            ?.key
        ?: trailers
            ?.firstOrNull {
                it.site.equals("YouTube", ignoreCase = true) &&
                        !it.key.isNullOrBlank()
            }
            ?.key
        .orEmpty()
}

private fun openTrailerInYouTube(context: Context, trailerKey: String): Boolean {
    val videoId = resolveYouTubeId(trailerKey)
    if (videoId.isBlank()) return false

    val appIntent = Intent(Intent.ACTION_VIEW, "vnd.youtube:$videoId".toUri())
    val webIntent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$videoId".toUri())

    return try {
        context.startActivity(appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: Exception) {
        try {
            context.startActivity(webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (_: Exception) {
            false
        }
    }
}

private fun openWatchProviderLink(context: Context, watchProviderInfo: WatchProviderInfo): Boolean {
    if (watchProviderInfo.link.isBlank()) return false
    // TMDB returns a provider landing page URL for the selected region.
    val intent = Intent(Intent.ACTION_VIEW, watchProviderInfo.link.toUri())
    return try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: Exception) {
        false
    }
}
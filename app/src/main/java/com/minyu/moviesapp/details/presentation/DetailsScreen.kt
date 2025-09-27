package com.minyu.moviesapp.details.presentation

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
import androidx.compose.material.icons.rounded.ImageNotSupported
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.minyu.moviesapp.R
import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity
import com.minyu.moviesapp.movieList.data.remote.MovieApi
import com.minyu.moviesapp.movieList.util.RatingBar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.text.SimpleDateFormat
import java.util.Locale

// Composable to display a Youtube trailer using the YouTubePlayerView
@Composable
fun YouTubeTrailerPlayer(trailerKey: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Use AndroidView to embed the YouTubePlayerView in Compose
    AndroidView(factory = { ctx ->
        YouTubePlayerView(ctx).apply {
            // Add a listener to handle YouTube player events
            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                // When the player is ready, load the video using the trailer key
                override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
                    youTubePlayer.loadVideo(trailerKey, 0f)
                }
                // If an error occurs, show a Toast message only
                override fun onError(
                    youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer,
                    error: PlayerConstants.PlayerError
                ) {
                    android.widget.Toast.makeText(
                        context,
                        "Trailer cannot be played here. Please use the Watch on YouTube button.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    },
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}

// Helper function to format release date nicely
fun formatReleaseDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = parser.parse(dateString)
        if (date != null) formatter.format(date) else dateString
    } catch (e: Exception) {
        dateString
    }
}

// Main details screen composable
@Composable
fun DetailsScreen(navController: NavController) {
    // Get ViewModel and state
    val detailsViewModel = hiltViewModel<DetailsViewModel>()
    val detailsState = detailsViewModel.detailsState.collectAsState().value
    val reviews by detailsViewModel.reviews.collectAsState(emptyList<MovieReviewEntity>())

    // Load images using Coil for backdrop and poster
    val backDropImageState = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(MovieApi.IMAGE_BASE_URL + detailsState.movie?.backdrop_path)
            .size(Size.ORIGINAL)
            .build()
    ).state
    val posterImageState = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(MovieApi.IMAGE_BASE_URL + detailsState.movie?.poster_path)
            .size(Size.ORIGINAL)
            .build()
    ).state

    var reviewText by remember { mutableStateOf("") }

    // Main vertical layout with scroll
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar with back button and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = detailsState.movie?.title ?: "",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Backdrop image or placeholder
        if (backDropImageState is AsyncImagePainter.State.Error) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(70.dp),
                    imageVector = Icons.Rounded.ImageNotSupported,
                    contentDescription = detailsState.movie?.title
                )
            }
        }
        // Display backdrop image if loading is successful
        if (backDropImageState is AsyncImagePainter.State.Success) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                painter = backDropImageState.painter,
                contentDescription = detailsState.movie?.title,
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Row containing poster image and movie details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Poster image or placeholder
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(240.dp)
            ) {
                // Display placeholder or error message if poster image loading fails
                if (posterImageState is AsyncImagePainter.State.Error) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(70.dp),
                            imageVector = Icons.Rounded.ImageNotSupported,
                            contentDescription = detailsState.movie?.title
                        )
                    }
                }
                // Display poster image if loading is successful
                if (posterImageState is AsyncImagePainter.State.Success) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        painter = posterImageState.painter,
                        contentDescription = detailsState.movie?.title,
                        contentScale = ContentScale.Crop
                    )
                }
            }
            // Movie details column
            detailsState.movie?.let { movie ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = movie.title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Row containing rating and rating value
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp)
                    ) {
                        RatingBar(
                            starsModifier = Modifier.size(18.dp),
                            rating = movie.vote_average / 2
                        )
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = movie.vote_average.toString().take(3),
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            maxLines = 1,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = "${stringResource(R.string.language)} ${movie.original_language}"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = stringResource(R.string.release_date) + " " + formatReleaseDate(movie.release_date)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        // Show the votes label followed by the rating value
                        text = stringResource(R.string.votes) + ": " + movie.vote_average.toString().take(3)
                    )
                    Button(
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                        enabled = detailsState.movie.title.isNotBlank(),
                        onClick = {
                            detailsViewModel.addFavoriteMovie(
                                movieId = movie.id,
                                title = movie.title,
                                posterUrl = movie.poster_path
                            )
                            navController.navigate("favorite_movies")
                        }
                    ) {
                        Text("Add to Favorites")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        // Display section title for movie overview
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(R.string.overview),
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Display movie overview
        detailsState.movie?.let {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = it.overview,
                fontSize = 16.sp,
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        // Trailers section: embed trailer and provide fallback button
        val validTrailer = detailsState.movie?.trailers?.firstOrNull { it.key?.isNotBlank() == true }
        if (validTrailer != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                text = "Trailer",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold
            )
            // Embed the YouTube trailer
            YouTubeTrailerPlayer(
                trailerKey = validTrailer.key ?: "",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Always show a fallback button to open in YouTube app/browser
            val context = LocalContext.current
            Button(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = {
                    val appIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("vnd.youtube:${validTrailer.key}"))
                    val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com/watch?v=${validTrailer.key}"))
                    try {
                        context.startActivity(appIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (_: android.content.ActivityNotFoundException) {
                        context.startActivity(webIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
            ) {
                Text("Watch on YouTube")
            }
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Spacer(modifier = Modifier.height(24.dp))
            // Show a message if no trailer is available
            Text(
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                text = "Trailer not available",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Add Review Section
        Text(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            text = "Add a Review",
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("Your review") },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )

        Button(
            onClick = {
                detailsState.movie?.let { movie ->
                    detailsViewModel.insertReview(
                        MovieReviewEntity(
                           movieId = movie.id,
                            userName = "YourUserName",
                            rating = 4.5f,
                            comment = reviewText,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    reviewText = ""
                }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            enabled = reviewText.isNotBlank()
        ) {
            Text("Submit Review")
        }

        // Display submitted reviews
        Text(
            modifier = Modifier.padding(start = 16.dp, top = 24.dp),
            text = "Reviews",
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold
        )

        reviews.forEach { review ->
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = review.userName, fontWeight = FontWeight.Bold)
                Text(text = "Rating: ${review.rating}")
                Text(text = review.comment)
                Text(
                    text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(review.timestamp)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                // Add Delete button
                Button(
                    onClick = { detailsViewModel.deleteReview(review.id)},
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Delete")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }


        // Add extra space at the bottom to avoid collision with the navigation bar
        Spacer(modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()))
    }
}
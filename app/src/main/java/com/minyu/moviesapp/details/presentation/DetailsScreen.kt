package com.minyu.moviesapp.details.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ImageNotSupported
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.minyu.moviesapp.R
import com.minyu.moviesapp.movieList.data.remote.MovieApi
import com.minyu.moviesapp.movieList.util.RatingBar

@Composable
fun DetailsScreen() {

    // Obtain the DetailsViewModel using Hilt dependency injection
    val detailsViewModel = hiltViewModel<DetailsViewModel>()

    // Collect the current state of the DetailsViewModel
    val detailsState = detailsViewModel.detailsState.collectAsState().value

    // Fetch the backdrop image using Coil and remember its state
    val backDropImageState = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(MovieApi.IMAGE_BASE_URL + detailsState.movie?.backdrop_path)
            .size(Size.ORIGINAL)
            .build()
    ).state

    // Fetch the poster image using Coil and remember its state
    val posterImageState = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(MovieApi.IMAGE_BASE_URL + detailsState.movie?.poster_path)
            .size(Size.ORIGINAL)
            .build()

    ).state

    // Column that represents the entire details screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // Display a placeholder or error message if backdrop image loading fails
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

        // Spacer for visual separation
        Spacer(modifier = Modifier.height(16.dp))

        // Row containing poster image and movie details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Box for displaying poster image or error message
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(240.dp)
            ) {
                // Display placeholder or error message if poser image loading fails
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
                // Display poser image if loading is successful
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

            // Display movie details in a column
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

                    // Spacer for visual separation
                    Spacer(modifier = Modifier.height(16.dp))

                    // Row containing rating and rating value
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp)
                    ) {
                        // Custom Ratingbar component
                        RatingBar(
                            starsModifier = Modifier.size(18.dp),
                            rating = movie.vote_average / 2
                        )

                        // Display the numeric rating value
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = movie.vote_average.toString().take(3),
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            maxLines = 1,
                        )
                    }

                    // Spacer for visual separation
                    Spacer(modifier = Modifier.height(12.dp))

                    // Display original language
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = "${stringResource(R.string.language)} ${movie.original_language}"
                    )

                    // Spacer for visual separation
                    Spacer(modifier = Modifier.height(10.dp))

                    // Display release date
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = stringResource(R.string.release_date) + movie.release_date
                    )

                    // Spacer for visual separation
                    Spacer(modifier = Modifier.height(12.dp))

                    // Display release date and votes
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = "${movie.release_date} ${stringResource(R.string.votes)}"
                    )

                    // Add to Favorites Button
                    Button(
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                        enabled = detailsState.movie.title.isNotBlank(),
                        onClick = {
                            detailsViewModel.addFavoriteMovie(
                                movieId = movie.id,
                                title = movie.title,
                                posterUrl = movie.poster_path
                            )
                        }
                    ) {
                        Text("Add to Favorites")
                    }
                }
            }
        }

        // Spacer for visual separation
        Spacer(modifier = Modifier.height(32.dp))

        // Display section title for movie overview
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(R.string.overview),
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Spacer for visual separation
        Spacer(modifier = Modifier.height(8.dp))

        // Display movie overview
        detailsState.movie?.let {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = it.overview,
                fontSize = 16.sp,
            )
        }

        // Spacer for visual separation
        Spacer(modifier = Modifier.height(32.dp))
    }
}
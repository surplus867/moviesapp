package com.minyu.moviesapp.movieList.domain.repository

import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity
import com.minyu.moviesapp.movieList.data.remote.respond.LocalizedDetailsDto
import com.minyu.moviesapp.movieList.data.remote.respond.MovieCreditsDto
import com.minyu.moviesapp.movieList.data.remote.respond.TrailerDto
import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.domain.model.WatchProviderInfo
import com.minyu.moviesapp.movieList.util.Resource
import kotlinx.coroutines.flow.Flow

interface MovieListRepository {
    suspend fun getMovieList(
        forceFetchFromRemote: Boolean,
        category: String,
        page: Int
    ) : Flow<Resource<List<Movie>>>

    suspend fun getAsianMovieList(
        forceFetchFromRemote: Boolean,
        page: Int
    ) : Flow<Resource<List<Movie>>>

    suspend fun getMovie(id: Int, category: String? = null): Flow<Resource<Movie>>

    suspend fun getAsianDramaList(
        forceFetchFromRemote: Boolean,
        page: Int
    ): Flow<Resource<List<Movie>>>
    suspend fun addFavoriteMovie(movieId: Int, title: String, posterUrl: String)

    suspend fun getMovieTrailers(movieId: Int, isTv: Boolean = false): List<TrailerDto>

    suspend fun getWatchProviders(movieId: Int, region: String, isTv: Boolean = false): WatchProviderInfo?

    suspend fun getMovieCredits(movieId: Int, isTv: Boolean = false): MovieCreditsDto

    suspend fun getLocalizedDetails(id: Int, isTv: Boolean, language: String): LocalizedDetailsDto?

    suspend fun insertReview(review: MovieReviewEntity)

    fun getReviewsForMovie(movieId: Int): Flow<List<MovieReviewEntity>>

    suspend fun deleteReview(reviewId: Int)
}
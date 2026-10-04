package com.minyu.moviesapp.movieList.data.repository

import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.domain.repository.FavoriteMovieRepository
import javax.inject.Inject


class FavoriteMovieRepositoryImpl @Inject constructor() : FavoriteMovieRepository {

    private val favoriteMovies = mutableListOf<Movie>()

    override suspend fun addFavorite(movieId: Int, title: String, posterUrl: String, overview: String) {
        if (favoriteMovies.none { it.id == movieId }) {
            favoriteMovies.add(
                Movie(
                    id = movieId,
                    title = title,
                    posterPath = posterUrl,
                    backdropPath = "",
                    voteAverage = 0.0,
                    originalLanguage = "",
                    releaseDate = "",
                    overview = "",
                    adult = false,
                    genreIds = emptyList(),
                    originalTitle = "",
                    popularity = 0.0,
                    video = false,
                    voteCount = 0,
                    category = "",
                    dateAdded = System.currentTimeMillis(),
                    country = "",
                    region = ""
                )
            )
        }
    }

    override suspend fun isFavorite(movieId: Int): Boolean {
        return favoriteMovies.any { it.id == movieId }
    }

    override suspend fun getFavoriteMovies(): List<Movie> {
        return favoriteMovies.toList()
    }

    override suspend fun removeFavorite(movieId: Int) {
        favoriteMovies.removeAll { it.id == movieId }
    }
}
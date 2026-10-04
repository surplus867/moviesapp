package com.minyu.moviesapp.movieList.data.repository

import com.minyu.moviesapp.movieList.data.local.dao.FavoriteMovieDao
import com.minyu.moviesapp.movieList.data.local.entity.FavoriteMovieEntity
import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.domain.repository.FavoriteMovieRepository
import javax.inject.Inject

// Repository implementation for managing favorite movies in the database
class DatabaseFavoriteRepository @Inject constructor(
    private val favoriteMovieDao: FavoriteMovieDao
) : FavoriteMovieRepository {

    // Adds a movie to favorites, using a default overview if blank
    override suspend fun addFavorite(movieId: Int, title: String, posterUrl: String, overview: String) {
        val safeOverview = overview.ifBlank { "No overview available" }
        favoriteMovieDao.insertFavorite(
            FavoriteMovieEntity(movieId, title, safeOverview, posterUrl)
        )
    }

    // Adds a movie to favorites, using a default overview if blank
    override suspend fun isFavorite(movieId: Int): Boolean {
        return favoriteMovieDao.getAllFavorites().any { it.movieId == movieId }
    }

    // Retrieves favorites and keeps stored overview text for recommendation keyword matching.
    override suspend fun getFavoriteMovies(): List<Movie> {
       return favoriteMovieDao.getAllFavorites().map {
           Movie(
               id = it.movieId,
               title = it.title,
               posterPath = it.posterUrl,
               // Provide default or placeholder values for the rest:
               originalLanguage = "",
               originalTitle = "",
               overview = it.overview,
               popularity = 0.0,
               releaseDate = "",
               video = false,
               voteAverage = 0.0,
               voteCount = 0,
               category = "",
               genreIds = emptyList(),
               adult = false,
               backdropPath = "",
               dateAdded = System.currentTimeMillis(),
               country = "",
               region = ""
           )
       }
    }

    // Removes a movie from favorites by its ID
    override suspend fun removeFavorite(movieId: Int) {
        favoriteMovieDao.deleteFavoriteById(movieId)
    }
}
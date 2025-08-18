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

    // Retrieves all favorite movies, mapping database entities to domain model
    override suspend fun getFavoriteMovies(): List<Movie> {
       return favoriteMovieDao.getAllFavorites().map {
           Movie(
               id = it.movieId,
               title = it.title,
               poster_path = it.posterUrl,
               // Provide default or placeholder values for the rest:
               original_language = "",
               original_title = "",
               overview = "",
               popularity = 0.0,
               release_date = "",
               video = false,
               vote_average = 0.0,
               vote_count = 0,
               category = "",
               genre_ids = emptyList(),
               adult = false,
               backdrop_path = ""
           )
       }
    }

    // Removes a movie from favcrites by its ID
    override suspend fun removeFavorite(movieId: Int) {
        favoriteMovieDao.deleteFavoriteById(movieId)
    }
}
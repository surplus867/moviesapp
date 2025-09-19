// FavoriteMovieRepository.kt
// This interface defines the contract for managing favorite movies in the app.
// It provides methods to add, remove, check, and retrieve favorite movies.

package com.minyu.moviesapp.movieList.domain.repository

import com.minyu.moviesapp.movieList.domain.model.Movie

// Interface for favorite movie operations
interface FavoriteMovieRepository {
    // Adds a movie to the favorites list
    suspend fun addFavorite(movieId: Int, title: String, posterUrl: String, overview: String)

    // Removes a movie from the favorites list by its ID
    suspend fun removeFavorite(movieId: Int)

    // Checks if a movie is marked as favorite
    suspend fun isFavorite(movieId: Int): Boolean

    // Retrieves all favorite movies
    suspend fun getFavoriteMovies(): List<Movie>
}
package com.minyu.moviesapp.movieList.domain.repository

import com.minyu.moviesapp.movieList.domain.model.Movie


interface FavoriteMovieRepository {
    suspend fun addFavorite(movieId: Int, title: String, posterUrl: String, overview: String)
    suspend fun removeFavorite(movieId: Int)
    suspend fun isFavorite(movieId: Int): Boolean
    suspend fun getFavoriteMovies(): List<Movie>
}
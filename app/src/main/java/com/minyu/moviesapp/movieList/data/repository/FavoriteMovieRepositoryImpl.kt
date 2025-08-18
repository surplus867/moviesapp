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
                    poster_path = posterUrl,
                    backdrop_path = "",
                    vote_average = 0.0,
                    original_language = "",
                    release_date = "",
                    overview = "",
                    adult = false,
                    genre_ids = emptyList(),
                    original_title = "",
                    popularity = 0.0,
                    video = false,
                    vote_count = 0,
                    category = ""
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
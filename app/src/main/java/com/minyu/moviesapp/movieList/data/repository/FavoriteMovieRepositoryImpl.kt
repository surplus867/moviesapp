package com.minyu.moviesapp.movieList.data.repository

import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.domain.repository.FavoriteMovieRepository
import javax.inject.Inject


class FavoriteMovieRepositoryImpl @Inject constructor(

) : FavoriteMovieRepository {
    override suspend fun addFavorite(movieId: Int, title: String, posterUrl: String) {

    }

    override suspend fun isFavorite(movieId: Int): Boolean {
        TODO("Not yet implemented")
        return false
    }

    override suspend fun getFavoriteMovies(): List<Movie> {
        TODO("Not yet implemented")
        return emptyList()
    }
}
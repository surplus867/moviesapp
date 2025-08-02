package com.minyu.moviesapp.movieList.domain.repository

import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.util.Resource
import kotlinx.coroutines.flow.Flow

interface MovieListRepository {
    suspend fun getMovieList(
        forceFetchFromRemote: Boolean,
        category: String,
        page: Int
    ) : Flow<Resource<List<Movie>>>

    suspend fun getMovie(id: Int): Flow<Resource<Movie>>
    suspend fun addFavoriteMovie(movieId: Int, title: String, posterUrl: String)
}
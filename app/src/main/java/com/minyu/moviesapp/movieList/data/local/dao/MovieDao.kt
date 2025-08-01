package com.minyu.moviesapp.movieList.data.local.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minyu.moviesapp.movieList.data.local.entity.FavoriteMovieEntity
import com.minyu.moviesapp.movieList.data.local.entity.MovieRatingEntity


interface MovieDao {
    // Favorites
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(movie: FavoriteMovieEntity)

    @Delete
    suspend fun removeFavorite(movie: FavoriteMovieEntity)

    @Query("SELECT * FROM favorite_movies")
    suspend fun getAllFavorites(): List<FavoriteMovieEntity>

    // Ratings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun rateMovie(rating: MovieRatingEntity)

    @Query("SELECT * FROM movie_ratings WHERE movieID = :movieId")
    suspend fun getRatingForMovie(movieId: Int): MovieRatingEntity?
}
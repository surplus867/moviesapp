package com.minyu.moviesapp.movieList.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.minyu.moviesapp.movieList.data.local.entity.FavoriteMovieEntity

@Dao
interface FavoriteMovieDao {
    @Insert
    suspend fun insertFavorite(movie: FavoriteMovieEntity)

    @Insert
    suspend fun insertFavoriteMovie(favoriteMovie: FavoriteMovieEntity)

    @Query("SELECT * FROM favorite_movies")
    suspend fun getAllFavorites(): List<FavoriteMovieEntity>

}
package com.minyu.moviesapp.movieList.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minyu.moviesapp.movieList.data.local.entity.FavoriteMovieEntity

@Dao
interface FavoriteMovieDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(movie: FavoriteMovieEntity)

    @Query("SELECT * FROM favorite_movies")
    suspend fun getAllFavorites(): List<FavoriteMovieEntity>

    @Query("DELETE FROM favorite_movies WHERE movieId = :movieId")
    suspend fun deleteFavoriteById(movieId: Int)

}
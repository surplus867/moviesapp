package com.minyu.moviesapp.movieList.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.minyu.moviesapp.movieList.data.local.entity.FavoriteMovieEntity
import com.minyu.moviesapp.movieList.data.local.entity.MovieRatingEntity
import com.minyu.moviesapp.movieList.data.local.movie.MovieEntity

@Dao
interface MovieDao {

    // Movies
    @Upsert
    suspend fun upsertMovieList(movieList: List<MovieEntity>)

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Int): MovieEntity

    @Query("SELECT * FROM movies WHERE category = :category")
    suspend fun getMovieListByCategory(category: String): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE category = :category AND country = :country")
    suspend fun getMovieListByCategoryAndCountry(category: String, country: String): List<MovieEntity>

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

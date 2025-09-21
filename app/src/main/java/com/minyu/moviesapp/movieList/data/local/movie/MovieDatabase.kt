package com.minyu.moviesapp.movieList.data.local.movie

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minyu.moviesapp.movieList.data.local.dao.FavoriteMovieDao
import com.minyu.moviesapp.movieList.data.local.dao.MovieDao
import com.minyu.moviesapp.movieList.data.local.dao.MovieReviewDao
import com.minyu.moviesapp.movieList.data.local.entity.FavoriteMovieEntity
import com.minyu.moviesapp.movieList.data.local.entity.MovieRatingEntity
import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity

@Database(
    entities = [MovieEntity::class, FavoriteMovieEntity::class, MovieRatingEntity::class, MovieReviewEntity::class],
    version = 8,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun favoriteMovieDao(): FavoriteMovieDao

    abstract fun movieReviewDao(): MovieReviewDao
}
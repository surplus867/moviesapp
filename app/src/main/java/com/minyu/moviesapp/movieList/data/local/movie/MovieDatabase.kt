package com.minyu.moviesapp.movieList.data.local.movie

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minyu.moviesapp.movieList.data.local.entity.FavoriteMovieEntity
import com.minyu.moviesapp.movieList.data.local.entity.MovieRatingEntity

@Database(
    entities = [MovieEntity::class, FavoriteMovieEntity::class, MovieRatingEntity::class],
    version = 2
)
abstract class MovieDatabase : RoomDatabase() {
    abstract val movieDao: MovieDao
}
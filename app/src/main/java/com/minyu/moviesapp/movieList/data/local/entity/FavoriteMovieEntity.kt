package com.minyu.moviesapp.movieList.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey val movieId: Int,
    val title: String,
    val overview: String = "No overview available",
    val posterUrl: String
)
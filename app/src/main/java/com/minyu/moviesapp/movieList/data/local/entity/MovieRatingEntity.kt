package com.minyu.moviesapp.movieList.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_ratings")
data class MovieRatingEntity(
    @PrimaryKey val movieId: Int,
    val rating: Float
)

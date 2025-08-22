package com.minyu.moviesapp.movieList.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_reviews")
data class MovieReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val movieId: Int,
    val userName: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long
)
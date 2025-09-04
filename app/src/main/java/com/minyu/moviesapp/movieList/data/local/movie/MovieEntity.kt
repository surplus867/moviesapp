package com.minyu.moviesapp.movieList.data.local.movie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "movies",
    primaryKeys = ["id", "category", "country"]
    )
data class MovieEntity(
    val id: Int,
    val category: String,
    val country: String,
    val adult: Boolean,
    val backdrop_path: String,
    val genre_ids: String,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val dateAdded: Long,
    val poster_path: String,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int,
)
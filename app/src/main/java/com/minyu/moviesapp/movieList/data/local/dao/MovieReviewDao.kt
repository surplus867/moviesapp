package com.minyu.moviesapp.movieList.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity

@Dao
interface MovieReviewDao {
    @Insert
    suspend fun insertReview(review: MovieReviewEntity)

    @Query("SELECT * FROM movie_reviews WHERE movieId = :movieId ORDER BY timestamp DESC")
    suspend fun getReviewsForMovie(movieId: Int): List<MovieReviewEntity>
}
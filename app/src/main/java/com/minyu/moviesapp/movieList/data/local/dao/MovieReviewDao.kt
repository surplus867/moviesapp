package com.minyu.moviesapp.movieList.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: MovieReviewEntity)

    @Query("SELECT * FROM movie_reviews WHERE movieId = :movieId ORDER BY timestamp DESC")
    fun getReviewsForMovie(movieId: Int):Flow<List<MovieReviewEntity>>

    @Query("DELETE FROM movie_reviews WHERE id = :reviewId")
    suspend fun deleteReviewById(reviewId: Int)
}
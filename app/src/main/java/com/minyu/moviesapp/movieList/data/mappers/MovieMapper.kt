package com.minyu.moviesapp.movieList.data.mappers

import com.minyu.moviesapp.movieList.data.local.movie.MovieEntity
import com.minyu.moviesapp.movieList.data.remote.respond.MovieDto
import com.minyu.moviesapp.movieList.domain.model.Movie

// Maps MovieDto (from API) to MovieEntity (for local database)
fun MovieDto.toMovieEntity(
    category: String,
    country: String
): MovieEntity {
    return MovieEntity(
        adult = adult ?: false,
        backdrop_path = backdrop_path ?: "",
        original_language = original_language ?: "",
        overview = overview ?: "",
        poster_path = poster_path ?: "",
        release_date = release_date ?: "",
        title = title ?: "",
        vote_average = vote_average ?: 0.0,
        popularity = popularity ?: 0.0,
        vote_count = vote_count ?: 0,
        id = id,
        original_title = original_title ?: "",
        video = video ?: false,

        category = category,
        country = country,

        // Convert genre_ids list to comma-separated string, fallback on error
        genre_ids = try {
            genre_ids.joinToString(",")
        } catch (e: Exception) {
            "-1,-2"
        },
        // Store current time as dateAdded
        dateAdded = System.currentTimeMillis()
    )
}

// Maps MovieEntity (from local database) to Movie (domain model)
fun MovieEntity.toMovie(
    category: String,
    country: String
): Movie{
    return Movie(
        adult = adult,
        backdrop_path = backdrop_path,
        original_language = original_language,
        overview = overview,
        poster_path = poster_path,
        release_date = release_date,
        title = title,
        vote_average = vote_average,
        popularity = popularity,
        vote_count = vote_count,
        id = id,
        original_title = original_title,
        video = video,
        category = category,
        country = country,
        region = country,

        // Convert genre_ids string back to list of Ints, fallback on error
        genre_ids = try {
            genre_ids.split(",").map { it.toInt() }
        } catch (e: Exception) {
            listOf(-1, -2)
        },
        // Pass through dateAdded
        dateAdded = dateAdded
    )
}
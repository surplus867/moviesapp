package com.minyu.moviesapp.movieList.data.remote

import com.minyu.moviesapp.movieList.data.remote.respond.MovieListDto
import com.minyu.moviesapp.movieList.data.remote.respond.TrailerListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi {

    @GET("movie/{category}")
    suspend fun getMoviesList(
        @Path("category") category: String,
        @Query("page") page: Int,
        @Query("api_key") apikey: String = API_KEY
    ): MovieListDto

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieTrailers(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apikey: String = API_KEY
    ): TrailerListDto


    // Add this function to fetch Korean movies
    @GET("discover/movie")
    suspend fun getMoviesByLanguage(
        @Query("with_original_language") language: String = "ko",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int,
        @Query("api_key") apikey: String = API_KEY
    ): MovieListDto


    suspend fun getAsianDramas(
        language: String,
        page: Int,
        apikey: String = API_KEY
    ): MovieListDto = getMoviesByLanguageAndGenre(language, "18", page, apikey)

    @GET("discover/movie")
    suspend fun getMoviesByLanguageAndGenre(
        @Query("with_original_language") language: String,
        @Query("with_genres") genre: String,
        @Query("page") page: Int,
        @Query("api_key") apikey: String = API_KEY
    ): MovieListDto

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
        const val API_KEY = "05663a72e30277317e18431232478d06"
    }
}
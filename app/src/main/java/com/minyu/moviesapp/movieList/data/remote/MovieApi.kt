// MovieApi.kt
// This interface defines the remote API endpoints for fetching movies and trailers from TMDB.
// It includes methods for popular/upcoming movies, trailers, language/genre filtering, and Asian dramas.

package com.minyu.moviesapp.movieList.data.remote

import com.minyu.moviesapp.movieList.data.remote.respond.MovieListDto
import com.minyu.moviesapp.movieList.data.remote.respond.TrailerListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi {
    // Fetches a list of movies by category (e.g., popular, upcoming)
    @GET("movie/{category}")
    suspend fun getMoviesList(
        @Path("category") category: String,
        @Query("page") page: Int,
        @Query("api_key") apikey: String = API_KEY
    ): MovieListDto

    // Fetches trailers for a specific movie by its ID
    @GET("movie/{movie_id}/videos")
    suspend fun getMovieTrailers(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apikey: String = API_KEY
    ): TrailerListDto
                           
    // Fetches movies by original language (e.g., Korean, Chinese, Japanese)
    @GET("discover/movie")
    suspend fun getMoviesByLanguage(
        @Query("with_original_language") language: String = "ko",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int,
        @Query("api_key") apikey: String = API_KEY
    ): MovieListDto

    // Fetches Asian dramas by language and genre (genre 18 = Drama)
    // This is a helper function that calls getMoviesByLanguageAndGenre
    suspend fun getAsianDramas(
        language: String,
        page: Int,
        apikey: String = API_KEY
    ): MovieListDto = getMoviesByLanguageAndGenre(language, "18", page, apikey)

    // Fetches movies by both language and genre
    @GET("discover/movie")
    suspend fun getMoviesByLanguageAndGenre(
        @Query("with_original_language") language: String,
        @Query("with_genres") genre: String,
        @Query("page") page: Int,
        @Query("api_key") apikey: String = API_KEY
    ): MovieListDto

    companion object {
        // Base URL for TMDB API
        const val BASE_URL = "https://api.themoviedb.org/3/"
        // Base URL for movie images
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
        // API key for authentication (should be set securely)
        const val API_KEY = "05663a72e30277317e18431232478d06"
    }
}
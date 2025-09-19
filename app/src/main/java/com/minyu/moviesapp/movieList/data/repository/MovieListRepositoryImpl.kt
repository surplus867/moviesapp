package com.minyu.moviesapp.movieList.data.repository


import android.util.Log
import com.minyu.moviesapp.movieList.data.local.entity.FavoriteMovieEntity
import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity
import com.minyu.moviesapp.movieList.data.local.movie.MovieDatabase
import com.minyu.moviesapp.movieList.data.local.movie.MovieEntity
import com.minyu.moviesapp.movieList.data.mappers.toMovie
import com.minyu.moviesapp.movieList.data.mappers.toMovieEntity
import com.minyu.moviesapp.movieList.data.remote.MovieApi
import com.minyu.moviesapp.movieList.data.remote.respond.TrailerDto
import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.domain.repository.MovieListRepository
import com.minyu.moviesapp.movieList.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject

class MovieListRepositoryImpl @Inject constructor(
    private val movieApi: MovieApi,
    private val movieDatabase: MovieDatabase
) : MovieListRepository {

    // Fetch a list of movies from either the local database or the remote API
    override suspend fun getMovieList(
        forceFetchFromRemote: Boolean,
        category: String,
        page: Int
    ): Flow<Resource<List<Movie>>> {
        return flow {
            // Emit loading state to notify the UI that data is being fetched
            emit(Resource.Loading(true))

            // Try fetching movies from the local database
            val localMovieList = movieDatabase.movieDao().getMovieListByCategory(category)

            // Check if local data should be used and emit the result
            val shouldLoadLocalMovie = localMovieList.isNotEmpty() && !forceFetchFromRemote
            if (shouldLoadLocalMovie) {
                emit(Resource.Success(data = localMovieList.map {
                    it.toMovie(
                        category,
                        country = it.country
                    )
                }))

                // Emit loading state to notify the UI that data fetching is complete
                emit(Resource.Loading(false))
                return@flow
            }

            // Fetch movies from the remote API
            val movieListFromApi = try {
                movieApi.getMoviesList(category, page)
            } catch (e: IOException) {
                // Handle network or IO errors
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading movies"))
                return@flow
            } catch (e: HttpException) {
                // Handle HTTP-related errors
                e.printStackTrace()
                emit(Resource.Error(message = "Error Loading movies"))
                return@flow
            } catch (e: Exception) {
                // Handle other unexpected errors
                e.printStackTrace()
                emit(Resource.Error(message = "Error Loading movies"))
                return@flow
            }

            // Transform the API response and update the local database
            val movieEntities = movieListFromApi.results.let {
                it.map { movieDto ->
                    movieDto.toMovieEntity(category, country = "")
                }
            }

            movieDatabase.movieDao().upsertMovieList(movieEntities)

            // Emit the transformed data to the UI
            emit(
                Resource.Success(
                    movieEntities.map { it.toMovie(category, country = "") }
                ))
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getAsianMovieList(
        forceFetchFromRemote: Boolean,
        page: Int
    ): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading(true))

        val asianCountries = listOf("Japan", "China", "Korea")
        val asianCategories = listOf("japanese", "chinese", "korean")
        val asianLanguageCodes = listOf("ja", "zh", "ko")

        // Try local cache first
        val localMovieList = asianCategories.zip(asianCountries).flatMap { (category, country) ->
            movieDatabase.movieDao().getMovieListByCategoryAndCountry(category, country)
        }

        if (localMovieList.isNotEmpty() && !forceFetchFromRemote) {
            emit(
                Resource.Success(
                    localMovieList.map { it.toMovie(it.category, country = it.country) }
                )
            )
            emit(Resource.Loading(false))
            return@flow
        }

        val aggregatedEntities = mutableListOf<MovieEntity>()

        // Fetch each region and aggregate
        for (i in asianCategories.indices) {
            val category = asianCategories[i]
            val country = asianCountries[i]
            val lang = asianLanguageCodes[i]

            try {
                val response = movieApi.getMoviesByLanguage(
                    language = lang,
                    page = page,
                )
                val entities = response.results.map { dto ->
                    dto.toMovieEntity(category = category, country = country)
                }
                aggregatedEntities += entities
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading $country movies"))
                emit(Resource.Loading(false))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading $country movies"))
                emit(Resource.Loading(false))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading $country movies"))
                emit(Resource.Loading(false))
                return@flow
            }
        }

        // Save all at once
        if (aggregatedEntities.isNotEmpty()) {
            movieDatabase.movieDao().upsertMovieList(aggregatedEntities)
        }

        emit(
            Resource.Success(
                aggregatedEntities.map { it.toMovie(category = it.category, country = it.country) }
            )
        )
        emit(Resource.Loading(false))
    }

    // Fetch a single movie by its ID from either the local database or emit an error if not found
    override suspend fun getMovie(id: Int): Flow<Resource<Movie>> {
        return flow {
            // Emit loading state to notify the UI that data is being fetched
            emit(Resource.Loading(true))

            // Try fetching the movie from the local database
            val movieEntity = movieDatabase.movieDao().getMovieById(id)

            // Check if the movie was found in the local database and emit the result
            emit(
                Resource.Success(movieEntity.toMovie(movieEntity.category, country = ""))
            )

            // Emit loading state to notify the UI that data fetching is complete
            emit(Resource.Loading(false))
            return@flow

        }
    }

     override suspend fun getAsianDramaList(
        forceFetchFromRemote: Boolean,
        page: Int
    ): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading(true))

        val asianCountries = listOf("Japan", "China", "Korea")
        val asianCategories = listOf("japanese drama", "chinese drama", "korean drama")
        val asianLanguageCodes = listOf("ja", "zh", "ko")

        // Try local cache first
        val localDramaList = asianCountries.zip(asianCountries).flatMap { (category, country) ->
            movieDatabase.movieDao().getMovieListByCategoryAndCountry(category, country)
        }

        if (localDramaList.isNotEmpty() && !forceFetchFromRemote) {
            emit(Resource.Success(localDramaList.map { it.toMovie(it.category, it.country) }))
            emit(Resource.Loading(false))
            return@flow
        }

        val aggregatedEntities = mutableListOf<MovieEntity>()

        for(i in asianCountries.indices) {
            val category = asianCategories[i]
            val country = asianCountries[i]
            val lang = asianLanguageCodes[i]

            try {
                val response = movieApi.getMoviesByLanguageAndGenre(
                    language = lang,
                    genre = "18",
                    page = page
                )
                val entities = response.results.map { dto ->
                    dto.toMovieEntity(category = category, country = country)
                }
                aggregatedEntities.addAll(entities)
            } catch (e: Exception) {
                emit(Resource.Error(message = "Error loading $country dramas"))
                emit(Resource.Loading(false))
                return@flow
            }
        }

        if (aggregatedEntities.isNotEmpty()) {
            movieDatabase.movieDao().upsertMovieList(aggregatedEntities)
        }

        emit(Resource.Success(aggregatedEntities.map { it.toMovie(it.category, it.country) }))
        emit(Resource.Loading(false))
    }



    override suspend fun addFavoriteMovie(movieId: Int, title: String, posterUrl: String) {
        val favoriteMovieEntity = FavoriteMovieEntity(
            movieId = movieId,
            title = title,
            overview = "",
            posterUrl = posterUrl
        )
        movieDatabase.favoriteMovieDao().insertFavorite(favoriteMovieEntity)
    }

    override suspend fun getMovieTrailers(movieId: Int): List<TrailerDto> {
        val trailers = movieApi.getMovieTrailers(movieId).results
        Log.d("Test", "Fetched trailers: ${trailers.size}")
        return trailers
    }

    override suspend fun insertReview(review: MovieReviewEntity) {
        movieDatabase.movieReviewDao().insertReview(review)
    }

    override fun getReviewsForMovie(movieId: Int): Flow<List<MovieReviewEntity>> {
        return movieDatabase.movieReviewDao().getReviewsForMovie(movieId)
    }

    override suspend fun deleteReview(reviewId: Int) {
        movieDatabase.movieReviewDao().deleteReviewById(reviewId)

    }
}
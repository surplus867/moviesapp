package com.minyu.moviesapp.movieList.data.repository


import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.minyu.moviesapp.movieList.data.local.entity.FavoriteMovieEntity
import com.minyu.moviesapp.movieList.data.local.entity.MovieReviewEntity
import com.minyu.moviesapp.movieList.data.local.movie.MovieDatabase
import com.minyu.moviesapp.movieList.data.local.movie.MovieEntity
import com.minyu.moviesapp.movieList.data.mappers.toMovie
import com.minyu.moviesapp.movieList.data.mappers.toMovieEntity
import com.minyu.moviesapp.movieList.data.remote.MovieApi
import com.minyu.moviesapp.movieList.data.remote.respond.LocalizedDetailsDto
import com.minyu.moviesapp.movieList.data.remote.respond.MovieCreditsDto
import com.minyu.moviesapp.movieList.data.remote.respond.TrailerDto
import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.domain.model.WatchProviderInfo
import com.minyu.moviesapp.movieList.domain.repository.MovieListRepository
import com.minyu.moviesapp.movieList.util.Resource
import com.minyu.moviesapp.core.LanguagePrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject

class MovieListRepositoryImpl @Inject constructor(
    private val movieApi: MovieApi,
    private val movieDatabase: MovieDatabase,
    @ApplicationContext private val context: Context
) : MovieListRepository {

    // Short TTL prevents stale local cache from hiding newly released titles.
    private val movieCacheTtlMillis = TimeUnit.HOURS.toMillis(6)

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
            val shouldLoadLocalMovie = localMovieList.isNotEmpty() &&
                    !forceFetchFromRemote &&
                    isCachedInCurrentLanguage(category) &&
                    isCacheFresh(localMovieList.map { it.dateAdded })
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
                movieApi.getMoviesList(category, page, displayLanguage = currentLanguage())
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
            markCachedLanguage(category)

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

        if (localMovieList.isNotEmpty() &&
            !forceFetchFromRemote &&
            isCachedInCurrentLanguage("asianMovies") &&
            isCacheFresh(localMovieList.map { it.dateAdded })
        ) {
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
                    displayLanguage = currentLanguage()
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
            markCachedLanguage("asianMovies")
        }

        emit(
            Resource.Success(
                aggregatedEntities.map { it.toMovie(category = it.category, country = it.country) }
            )
        )
        emit(Resource.Loading(false))
    }

    // Fetch a single movie by its ID from either the local database or emit an error if not found
    override suspend fun getMovie(id: Int, category: String?): Flow<Resource<Movie>> {
        return flow {
            // Emit loading state to notify the UI that data is being fetched
            emit(Resource.Loading(true))

            // Try fetching the movie from the local database
            val movieEntity = category?.let { movieDatabase.movieDao().getMovieByIdAndCategory(id, it) }
                ?: movieDatabase.movieDao().getMovieById(id)

            // Check if the movie was found in the local database and emit the result
            emit(
                Resource.Success(movieEntity.toMovie(movieEntity.category, country = ""))
            )

            // Emit loading state to notify the UI that data fetching is complete
            emit(Resource.Loading(false))
            return@flow

        }
    }

     @RequiresApi(Build.VERSION_CODES.O)
     override suspend fun getAsianDramaList(
        forceFetchFromRemote: Boolean,
        page: Int
    ): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading(true))

        val asianCountries = listOf("Japan", "China", "Korea")
        val asianCategories = listOf("japanese drama", "chinese drama", "korean drama")
        val asianLanguageCodes = listOf("ja", "zh", "ko")

        // Try local cache first
        val localDramaList = asianCategories.zip(asianCountries).flatMap { (category, country) ->
            movieDatabase.movieDao().getMovieListByCategoryAndCountry(category, country)
        }

        if (localDramaList.isNotEmpty() &&
            !forceFetchFromRemote &&
            isCachedInCurrentLanguage("asianDramas") &&
            isCacheFresh(localDramaList.map { it.dateAdded })
        ) {
            emit(Resource.Success(localDramaList.map { it.toMovie(it.category, it.country) }))
            emit(Resource.Loading(false))
            return@flow
        }

        val aggregatedEntities = mutableListOf<MovieEntity>()

         for (i in asianCountries.indices) {
             val category = asianCategories[i]
             val country = asianCountries[i]
             val lang = asianLanguageCodes[i]

             try {
                 val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(java.util.Date())
                 val response = movieApi.discoverTvShows(
                     language = lang,
                     airedBefore = today,
                     page = page,
                     displayLanguage = currentLanguage()
                 )
                 val entities = response.results.map { it.toMovieEntity(category, country) }
                 aggregatedEntities.addAll(entities)
             } catch (e: Exception) {
                 emit(Resource.Error(message = "Error loading $country dramas"))
                 emit(Resource.Loading(false))
                 return@flow
             }
         }

        if (aggregatedEntities.isNotEmpty()) {
            movieDatabase.movieDao().upsertMovieList(aggregatedEntities)
            markCachedLanguage("asianDramas")
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

    override suspend fun getMovieTrailers(movieId: Int, isTv: Boolean): List<TrailerDto> {
        val trailers = try {
            if (isTv) movieApi.getTvTrailers(movieId).results
            else movieApi.getMovieTrailers(movieId).results
        } catch (e: IOException) {
            emptyList()
        } catch (e: HttpException) {
            emptyList()
        }
        Log.d("Test", "Fetched trailers: ${trailers.size}")
        return trailers
    }

    override suspend fun getWatchProviders(movieId: Int, region: String, isTv: Boolean): WatchProviderInfo? {
        return try {
            val response = if (isTv) movieApi.getTvWatchProviders(movieId)
            else movieApi.getWatchProviders(movieId)
            val upperRegion = region.uppercase()
            // Prefer the user's region; fall back to US where coverage is usually present.
            val countryProviders = response.results?.get(upperRegion)
                ?: response.results?.get("US")
                ?: return null

            // Flatten streaming/rent/buy into one display list for a simple UI section.
            val providerNames = buildList {
                countryProviders.flatrate?.forEach { provider ->
                    provider.provider_name?.takeIf { it.isNotBlank() }?.let { add(it) }
                }
                countryProviders.rent?.forEach { provider ->
                    provider.provider_name?.takeIf { it.isNotBlank() }?.let { add(it) }
                }
                countryProviders.buy?.forEach { provider ->
                    provider.provider_name?.takeIf { it.isNotBlank() }?.let { add(it) }
                }
            }.distinct()

            val link = countryProviders.link.orEmpty()
            if (providerNames.isEmpty() && link.isBlank()) return null

            WatchProviderInfo(
                region = if (response.results?.containsKey(upperRegion) == true) upperRegion else "US",
                link = link,
                providers = providerNames
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getMovieCredits(movieId: Int, isTv: Boolean): MovieCreditsDto {
        return try {
            if (isTv) movieApi.getTvCredits(movieId) else movieApi.getMovieCredits(movieId)
        } catch (e: IOException) {
            MovieCreditsDto()
        } catch (e: HttpException) {
            MovieCreditsDto()
        }
    }

    override suspend fun getLocalizedDetails(
        id: Int,
        isTv: Boolean,
        language: String
    ): LocalizedDetailsDto? {
        return try {
            if (isTv) movieApi.getTvDetails(id, language) else movieApi.getMovieDetails(id, language)
        } catch (e: IOException) {
            null
        } catch (e: HttpException) {
            null
        }
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

    private fun currentLanguage(): String =
        LanguagePrefs.get(context).ifBlank { Locale.getDefault().toLanguageTag() }

    private val cacheLanguagePrefs by lazy {
        context.getSharedPreferences("list_cache_language", Context.MODE_PRIVATE)
    }

    // Cached rows keep the language they were fetched in, so refetch when the app language changes.
    private fun isCachedInCurrentLanguage(key: String): Boolean =
        cacheLanguagePrefs.getString(key, null) == currentLanguage()

    private fun markCachedLanguage(key: String) {
        cacheLanguagePrefs.edit().putString(key, currentLanguage()).apply()
    }

    private fun isCacheFresh(dateAddedValues: List<Long>): Boolean {
        val newestCachedAt = dateAddedValues.maxOrNull() ?: return false
        return System.currentTimeMillis() - newestCachedAt < movieCacheTtlMillis
    }
}
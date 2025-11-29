package com.minyu.moviesapp.di

import com.minyu.moviesapp.core.data.TranslateRepository
import com.minyu.moviesapp.core.data.TranslateRepositoryImpl
import com.minyu.moviesapp.movieList.data.local.dao.FavoriteMovieDao
import com.minyu.moviesapp.movieList.data.local.movie.MovieDatabase
import com.minyu.moviesapp.movieList.data.repository.DatabaseFavoriteRepository
import com.minyu.moviesapp.movieList.data.repository.FavoriteMovieRepositoryImpl
import com.minyu.moviesapp.movieList.data.repository.MovieListRepositoryImpl
import com.minyu.moviesapp.movieList.domain.repository.FavoriteMovieRepository
import com.minyu.moviesapp.movieList.domain.repository.MovieListRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMovieListRepository(
        movieListRepositoryImpl: MovieListRepositoryImpl
    ): MovieListRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteMovieRepository(
        databaseFavoriteRepository: DatabaseFavoriteRepository
    ): FavoriteMovieRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    @Provides
    @Singleton
    fun provideFavoriteMovieDao(database: MovieDatabase): FavoriteMovieDao {
        return database.favoriteMovieDao()
    }

    @Provides
    @Singleton
    fun provideTranslateRepository(): TranslateRepository {
        return TranslateRepositoryImpl()
    }
}
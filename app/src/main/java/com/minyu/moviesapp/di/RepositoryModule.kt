package com.minyu.moviesapp.di

import com.minyu.moviesapp.movieList.data.repository.FavoriteMovieRepositoryImpl
import com.minyu.moviesapp.movieList.data.repository.MovieListRepositoryImpl
import com.minyu.moviesapp.movieList.domain.repository.FavoriteMovieRepository
import com.minyu.moviesapp.movieList.domain.repository.MovieListRepository
import dagger.Binds
import dagger.Module
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
        favoriteMovieRepositoryImpl: FavoriteMovieRepositoryImpl
    ): FavoriteMovieRepository
}
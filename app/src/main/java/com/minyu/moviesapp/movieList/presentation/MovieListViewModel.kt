package com.minyu.moviesapp.movieList.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minyu.moviesapp.movieList.domain.model.Movie
import com.minyu.moviesapp.movieList.domain.repository.FavoriteMovieRepository
import com.minyu.moviesapp.movieList.domain.repository.MovieListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.minyu.moviesapp.movieList.util.Category
import com.minyu.moviesapp.movieList.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val movieListRepository: MovieListRepository,
    private val favoriteMovieRepository: FavoriteMovieRepository
) : ViewModel() {
    private var _movieListState = MutableStateFlow(MovieListState())
    val movieListState = _movieListState.asStateFlow()
    private var isPopularPaginationInFlight = false
    private var isUpcomingPaginationInFlight = false

    init {
        // Fetch popular and upcoming movie lists during ViewModel initialization
        getPoplarMovieList(false)
        getUpcomingMovieList(false)
    }

    // Function to handle UI events
    fun onEvent(event: MovieListUiEvent) {
        when (event) {
            // Toggle between popular and upcoming screens
            MovieListUiEvent.Navigate -> {
                _movieListState.update {
                    it.copy(
                        isCurrentPopularScreen = !movieListState.value.isCurrentPopularScreen
                    )
                }
            }

            // Paginate event triggers fetching more items
            is MovieListUiEvent.Paginate -> {
                if (event.category == Category.POPULAR) {
                    getPoplarMovieList(true)
                } else if (event.category == Category.UPCOMING) {
                    getUpcomingMovieList(true)
                }
            }
            is MovieListUiEvent.UpdateSearchQuery -> {
                _movieListState.update { it.copy(searchQuery = event.query) }
                refreshDiscoveryAndRecommendations()
            }
            is MovieListUiEvent.UpdateYearFilter -> {
                _movieListState.update { it.copy(selectedYear = event.year) }
                refreshDiscoveryAndRecommendations()
            }
            is MovieListUiEvent.UpdateLanguageFilter -> {
                _movieListState.update { it.copy(selectedLanguage = event.language) }
                refreshDiscoveryAndRecommendations()
            }
            is MovieListUiEvent.UpdateGenreFilter -> {
                _movieListState.update { it.copy(selectedGenre = event.genre) }
                refreshDiscoveryAndRecommendations()
            }
            MovieListUiEvent.ClearFilters -> {
                _movieListState.update {
                    it.copy(
                        searchQuery = "",
                        selectedYear = null,
                        selectedLanguage = null,
                        selectedGenre = null
                    )
                }
                refreshDiscoveryAndRecommendations()
            }
        }
    }

    // Function to fetch upcoming movie list
    private fun getPoplarMovieList(forceFetchFromRemote: Boolean) {
        if (isPopularPaginationInFlight) return
        viewModelScope.launch {
            isPopularPaginationInFlight = true
            _movieListState.update {
                it.copy(isLoading = true)
            }

            movieListRepository.getMovieList(
                forceFetchFromRemote,
                Category.POPULAR,
                movieListState.value.popularMovieListPage
            ).collectLatest { result ->
                when (result) {
                    is Resource.Error -> {
                        _movieListState.update {
                            it.copy(isLoading = false)
                        }
                        isPopularPaginationInFlight = false
                    }

                    is Resource.Success -> {
                        result.data?.let { popularList ->
                            val currentState = movieListState.value
                            val mergedPopular = mergeUniqueMovies(
                                currentState.popularMovieList,
                                popularList
                            )
                            // Prioritize "Popular" ownership for overlapping titles.
                            val prunedUpcoming = removeCrossListDuplicates(
                                primary = mergedPopular,
                                secondary = currentState.upcomingMovieList
                            )
                            _movieListState.update {
                                it.copy(
                                    popularMovieList = mergedPopular,
                                    upcomingMovieList = prunedUpcoming,
                                    popularMovieListPage = currentState.popularMovieListPage + 1
                                )
                            }
                            refreshDiscoveryAndRecommendations()
                        }
                        isPopularPaginationInFlight = false
                    }

                    is Resource.Loading -> {
                        _movieListState.update {
                            it.copy(isLoading = result.isLoading)
                        }
                        if (!result.isLoading) {
                            isPopularPaginationInFlight = false
                        }
                    }
                }
            }
        }
    }

    // Common function to handle movie list result and update state
    private fun getUpcomingMovieList(forceFetchFromRemote: Boolean) {
        if (isUpcomingPaginationInFlight) return
        viewModelScope.launch {
            isUpcomingPaginationInFlight = true
            _movieListState.update {
                it.copy(isLoading = true)
            }

            movieListRepository.getMovieList(
                forceFetchFromRemote,
                Category.UPCOMING,
                movieListState.value.upcomingMovieListPage
            ).collectLatest { result ->
                when (result) {
                    is Resource.Error -> {
                        _movieListState.update {
                            it.copy(isLoading = false)
                        }
                        isUpcomingPaginationInFlight = false
                    }

                    is Resource.Success -> {
                        result.data?.let { upcomingList ->
                            val currentState = movieListState.value
                            val mergedUpcoming = mergeUniqueMovies(
                                currentState.upcomingMovieList,
                                upcomingList
                            )
                            // Keep tabs visually distinct by removing ids already present in Popular.
                            val prunedUpcoming = removeCrossListDuplicates(
                                primary = currentState.popularMovieList,
                                secondary = mergedUpcoming
                            )
                            _movieListState.update {
                                it.copy(
                                    upcomingMovieList = prunedUpcoming,
                                    upcomingMovieListPage = currentState.upcomingMovieListPage + 1
                                )
                            }
                            refreshDiscoveryAndRecommendations()
                        }
                        isUpcomingPaginationInFlight = false
                    }
                    is Resource.Loading -> {
                        _movieListState.update {
                            it.copy(isLoading = result.isLoading)
                        }
                        if (!result.isLoading) {
                            isUpcomingPaginationInFlight = false
                        }
                    }
                }
            }
        }
    }

    private fun refreshDiscoveryAndRecommendations() {
        // Always recalculate filtered lists first so recommendation candidates can use them.
        refreshDiscoveryState()
        viewModelScope.launch {
            val state = movieListState.value
            val filteredCandidates = (state.filteredPopularMovieList + state.filteredUpcomingMovieList)
                .distinctBy(::movieIdentityKey)
            val candidates = if (filteredCandidates.isNotEmpty()) {
                filteredCandidates
            } else {
                (state.popularMovieList + state.upcomingMovieList).distinctBy(::movieIdentityKey)
            }

            val favoriteMovies = favoriteMovieRepository.getFavoriteMovies()
            val favoriteIds = favoriteMovies.map { it.id }.toSet()
            // Build a simple interest profile from favorite titles and overviews.
            val favoriteKeywords = favoriteMovies
                .flatMap { tokenizeForRecommendation(it.title) + tokenizeForRecommendation(it.overview) }
                .toSet()

            val recommendations = if (favoriteKeywords.isEmpty()) {
                // Cold start: rank by community signal (rating/popularity).
                candidates
                    .filterNot { it.id in favoriteIds }
                    .sortedWith(compareByDescending<Movie> { it.vote_average }.thenByDescending { it.popularity })
                    .take(10)
            } else {
                // Personalized path: score by keyword overlap, then break ties by popularity.
                candidates
                    .filterNot { it.id in favoriteIds }
                    .map { movie ->
                        val movieTokens = tokenizeForRecommendation(movie.title) + tokenizeForRecommendation(movie.overview)
                        val overlapScore = movieTokens.count { it in favoriteKeywords }
                        movie to (overlapScore * 10 + (movie.vote_average * 10).toInt())
                    }
                    .sortedWith(
                        compareByDescending<Pair<Movie, Int>> { it.second }
                            .thenByDescending { it.first.popularity }
                    )
                    .map { it.first }
                    .take(10)
            }

            _movieListState.update { it.copy(recommendedMovies = recommendations) }
        }
    }

    private fun refreshDiscoveryState() {
        val state = movieListState.value
        val allMovies = (state.popularMovieList + state.upcomingMovieList).distinctBy(::movieIdentityKey)

        // Year options are inferred from release_date prefix (yyyy).
        val availableYears = allMovies
            .mapNotNull { movie ->
                movie.release_date.takeIf { it.length >= 4 }?.take(4)?.takeIf { year ->
                    year.all(Char::isDigit)
                }
            }
            .distinct()
            .sortedDescending()

        val availableLanguages = allMovies
            .map { it.original_language.uppercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val availableGenres = allMovies
            .flatMap { movie ->
                movie.genre_ids.mapNotNull { genreId -> genreNameForId(genreId) }
            }
            .distinct()
            .sorted()

        val query = state.searchQuery.trim().lowercase(Locale.ROOT)
        val selectedLanguage = state.selectedLanguage?.lowercase(Locale.ROOT)
        val selectedGenre = state.selectedGenre

        // One predicate keeps all discovery dimensions aligned in both tabs.
        fun matches(movie: Movie): Boolean {
            val matchesQuery = query.isBlank() ||
                    movie.title.lowercase(Locale.ROOT).contains(query) ||
                    movie.overview.lowercase(Locale.ROOT).contains(query)
            val matchesYear = state.selectedYear == null ||
                    movie.release_date.startsWith(state.selectedYear)
            val matchesLanguage = selectedLanguage == null ||
                    movie.original_language.lowercase(Locale.ROOT) == selectedLanguage
            val matchesGenre = selectedGenre == null ||
                    movie.genre_ids.mapNotNull(::genreNameForId).contains(selectedGenre)
            return matchesQuery && matchesYear && matchesLanguage && matchesGenre
        }

        _movieListState.update {
            it.copy(
                availableYears = availableYears,
                availableLanguages = availableLanguages,
                availableGenres = availableGenres,
                filteredPopularMovieList = state.popularMovieList
                    .filter(::matches)
                    .distinctBy(::movieIdentityKey),
                filteredUpcomingMovieList = state.upcomingMovieList
                    .filter(::matches)
                    .distinctBy(::movieIdentityKey)
            )
        }
    }

    private fun genreNameForId(id: Int): String? {
        return when (id) {
            28 -> "Action"
            12 -> "Adventure"
            16 -> "Animation"
            35 -> "Comedy"
            80 -> "Crime"
            99 -> "Documentary"
            18 -> "Drama"
            10751 -> "Family"
            14 -> "Fantasy"
            36 -> "History"
            27 -> "Horror"
            10402 -> "Music"
            9648 -> "Mystery"
            10749 -> "Romance"
            878 -> "Science Fiction"
            10770 -> "TV Movie"
            53 -> "Thriller"
            10752 -> "War"
            37 -> "Western"
            else -> null
        }
    }

    private fun tokenizeForRecommendation(text: String): List<String> {
        // Keep only meaningful alphanumeric tokens to reduce noisy matches.
        return text
            .lowercase(Locale.ROOT)
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length >= 4 }
    }

    private fun mergeUniqueMovies(existing: List<Movie>, incoming: List<Movie>): List<Movie> {
        // Preserve current order, append only brand-new canonical movie identities.
        val seen = existing.map(::movieIdentityKey).toMutableSet()
        val appended = incoming.filter { seen.add(movieIdentityKey(it)) }
        return existing + appended
    }

    private fun removeCrossListDuplicates(primary: List<Movie>, secondary: List<Movie>): List<Movie> {
        val primaryKeys = primary.map(::movieIdentityKey).toHashSet()
        return secondary.filterNot { movieIdentityKey(it) in primaryKeys }
    }

    private fun movieIdentityKey(movie: Movie): String {
        val normalizedTitle = movie.title
            .ifBlank { movie.original_title }
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
        val year = movie.release_date
            .take(4)
            .takeIf { it.all(Char::isDigit) }
            .orEmpty()
        // Canonicalize by normalized title + year to collapse duplicate ids for the same film.
        return "$normalizedTitle|$year"
    }
}
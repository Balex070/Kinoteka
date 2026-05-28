package com.kinoteka.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kinoteka.app.data.model.Movie
import com.kinoteka.app.data.repository.MovieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MovieRepository = application.repository
    
    val allMovies: Flow<List<Movie>> = repository.allMovies
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    fun setGenreFilter(genre: String?) {
        _selectedGenre.value = genre
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun addMovie(movie: Movie) {
        viewModelScope.launch {
            repository.insert(movie)
        }
    }
    
    fun updateMovie(movie: Movie) {
        viewModelScope.launch {
            repository.update(movie)
        }
    }
    
    fun deleteMovie(movie: Movie) {
        viewModelScope.launch {
            repository.deleteImage(movie.posterPath)
            repository.delete(movie)
        }
    }
    
    fun getMovieById(id: Long): Movie? {
        return runBlocking {
            repository.getMovieById(id)
        }
    }
    
    fun toggleTheme() {
        // Theme toggling will be handled by the system or can be extended with DataStore
    }
}

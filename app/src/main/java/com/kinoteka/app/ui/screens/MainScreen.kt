package com.kinoteka.app.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kinoteka.app.data.model.Movie

@Composable
fun MainScreen(
    viewModel: MovieViewModel = viewModel()
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreenContent(
                viewModel = viewModel,
                onNavigateToAddMovie = {
                    navController.navigate("add_movie")
                },
                onNavigateToEditMovie = { movie ->
                    navController.navigate("edit_movie/${movie.id}")
                }
            )
        }
        
        composable("add_movie") {
            MovieFormScreen(
                movie = null,
                onSaveClick = { movie ->
                    viewModel.addMovie(movie)
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "edit_movie/{movieId}",
            arguments = listOf(androidx.navigation.navArgument("movieId") {
                type = androidx.navigation.NavType.LongType
            })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getLong("movieId") ?: return@composable
            
            val movie = remember(movieId) { 
                viewModel.getMovieById(movieId) 
            }
            
            LaunchedEffect(movie) {
                if (movie == null) {
                    navController.popBackStack()
                }
            }
            
            movie?.let {
                MovieFormScreen(
                    movie = it,
                    onSaveClick = { updatedMovie ->
                        viewModel.updateMovie(updatedMovie)
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    viewModel: MovieViewModel,
    onNavigateToAddMovie: () -> Unit,
    onNavigateToEditMovie: (Movie) -> Unit
) {
    val allMovies by viewModel.allMovies.collectAsState(initial = emptyList())
    val selectedGenre by viewModel.selectedGenre.collectAsState(initial = null)
    val searchQuery by viewModel.searchQuery.collectAsState(initial = "")
    
    val filteredMovies = remember(allMovies, selectedGenre, searchQuery) {
        allMovies.filter { movie ->
            val matchesGenre = selectedGenre == null || movie.genre == selectedGenre
            val matchesSearch = searchQuery.isEmpty() || 
                movie.title.contains(searchQuery, ignoreCase = true)
            matchesGenre && matchesSearch
        }
    }
    
    HomeScreen(
        movies = filteredMovies,
        selectedGenre = selectedGenre,
        searchQuery = searchQuery,
        onGenreSelected = { genre -> viewModel.setGenreFilter(genre) },
        onSearchQueryChanged = { query -> viewModel.setSearchQuery(query) },
        onAddMovieClick = onNavigateToAddMovie,
        onEditMovieClick = onNavigateToEditMovie,
        onDeleteMovieClick = { movie -> viewModel.deleteMovie(movie) },
        onToggleTheme = { viewModel.toggleTheme() }
    )
}

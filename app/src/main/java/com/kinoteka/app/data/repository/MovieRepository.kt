package com.kinoteka.app.data.repository

import android.content.Context
import android.net.Uri
import com.kinoteka.app.data.local.MovieDao
import com.kinoteka.app.data.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MovieRepository(private val movieDao: MovieDao, private val context: Context) {
    
    val allMovies: Flow<List<Movie>> = movieDao.getAllMovies()
    
    fun getMoviesByGenre(genre: String): Flow<List<Movie>> = movieDao.getMoviesByGenre(genre)
    
    fun searchMovies(query: String): Flow<List<Movie>> = movieDao.searchMovies(query)
    
    suspend fun insert(movie: Movie): Long = withContext(Dispatchers.IO) {
        movieDao.insert(movie)
    }
    
    suspend fun update(movie: Movie) = withContext(Dispatchers.IO) {
        movieDao.update(movie)
    }
    
    suspend fun delete(movie: Movie) = withContext(Dispatchers.IO) {
        movieDao.delete(movie)
    }
    
    suspend fun getMovieById(id: Long): Movie? = withContext(Dispatchers.IO) {
        movieDao.getMovieById(id)
    }
    
    suspend fun saveImageToInternalStorage(uri: Uri): String = withContext(Dispatchers.IO) {
        val fileName = "poster_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        file.absolutePath
    }
    
    suspend fun deleteImage(filePath: String?) = withContext(Dispatchers.IO) {
        filePath?.let {
            val file = File(it)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}

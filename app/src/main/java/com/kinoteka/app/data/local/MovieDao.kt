package com.kinoteka.app.data.local

import androidx.room.*
import com.kinoteka.app.data.model.Movie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY createdAt DESC")
    fun getAllMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE genre = :genre ORDER BY createdAt DESC")
    fun getMoviesByGenre(genre: String): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchMovies(query: String): Flow<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: Movie): Long

    @Update
    suspend fun update(movie: Movie)

    @Delete
    suspend fun delete(movie: Movie)

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Long): Movie?
}

package com.example.kinotheque.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.kinotheque.R
import com.example.kinotheque.data.Movie
import com.example.kinotheque.databinding.ActivityAddEditMovieBinding
import kotlinx.coroutines.launch

class AddEditMovieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditMovieBinding
    private val application by lazy { application as KinothequeApplication }
    
    private var movieId: Int = -1
    private var isEditMode = false
    private var posterUri: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            posterUri = it.toString()
            binding.imagePosterPreview.load(it) {
                crossfade(true)
            }
            binding.imagePosterPreview.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Проверяем, редактируем ли мы существующий фильм
        movieId = intent.getIntExtra("movie_id", -1)
        isEditMode = movieId != -1

        if (isEditMode) {
            loadMovieData()
        }

        setupClickListeners()
    }

    private fun loadMovieData() {
        posterUri = intent.getStringExtra("movie_poster")
        
        binding.editTextTitle.setText(intent.getStringExtra("movie_title"))
        binding.editTextYear.setText(intent.getStringExtra("movie_year").toString())
        binding.editTextDescription.setText(intent.getStringExtra("movie_description"))
        binding.editTextGenre.setText(intent.getStringExtra("movie_genre"))
        binding.ratingBar.rating = intent.getIntExtra("movie_rating", 0).toFloat()

        posterUri?.let {
            binding.imagePosterPreview.load(Uri.parse(it)) {
                crossfade(true)
            }
            binding.imagePosterPreview.visibility = android.view.View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        // Кнопка выбора изображения
        binding.buttonSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Кнопка закрытия (крестик)
        binding.buttonClose.setOnClickListener {
            finish()
        }

        // Кнопка сохранения
        binding.buttonSave.setOnClickListener {
            saveMovie()
        }
    }

    private fun saveMovie() {
        val title = binding.editTextTitle.text.toString().trim()
        val yearStr = binding.editTextYear.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val genre = binding.editTextGenre.text.toString().trim()
        val rating = binding.ratingBar.rating.toInt()

        // Валидация
        if (title.isEmpty()) {
            binding.editTextTitle.error = "Введите название"
            return
        }

        if (yearStr.isEmpty()) {
            binding.editTextYear.error = "Введите год"
            return
        }

        val year = yearStr.toIntOrNull()
        if (year == null || year < 1900 || year > 2100) {
            binding.editTextYear.error = "Некорректный год"
            return
        }

        if (genre.isEmpty()) {
            Toast.makeText(this, "Выберите жанр", Toast.LENGTH_SHORT).show()
            return
        }

        val movie = Movie(
            id = if (isEditMode) movieId else 0,
            title = title,
            year = year,
            description = description,
            genre = genre,
            rating = rating,
            posterPath = posterUri
        )

        lifecycleScope.launch {
            if (isEditMode) {
                application.repository.update(movie)
                Toast.makeText(this@AddEditMovieActivity, "Фильм обновлен", Toast.LENGTH_SHORT).show()
            } else {
                application.repository.insert(movie)
                Toast.makeText(this@AddEditMovieActivity, "Фильм добавлен", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}

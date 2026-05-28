package com.example.kinotheque.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filterable
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.kinotheque.R
import com.example.kinotheque.data.Movie
import com.example.kinotheque.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var movieAdapter: MovieAdapter
    private val application by lazy { application as KinothequeApplication }
    private var currentGenre = "Все"

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Для главного экрана выбор изображения не нужен
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupGenreChips()
        setupSearch()
        setupFab()
        setupThemeToggle()
        observeMovies()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(
            onItemClick = { movie -> /* Можно добавить детали */ },
            onEditClick = { movie -> openEditScreen(movie) },
            onDeleteClick = { movie -> deleteMovie(movie) }
        )
        binding.recyclerViewMovies.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMovies.adapter = movieAdapter
    }

    private fun setupGenreChips() {
        val genres = listOf(
            "Все", "Комедии", "Мультфильмы", "Ужасы", "Фантастика", 
            "Триллеры", "Боевики", "Мелодрамы", "Детективы", "Фэнтези", 
            "Аниме", "Документальные", "Детские", "Биографии", 
            "На реальных событиях", "Ток-шоу"
        )

        genres.forEach { genre ->
            val chip = Chip(this).apply {
                text = genre
                isCheckable = true
                setOnClickListener {
                    currentGenre = genre
                    observeMovies()
                }
            }
            binding.chipGroupGenres.addView(chip)
        }
        
        // Выбрать "Все" по умолчанию
        (binding.chipGroupGenres.getChildAt(0) as? Chip)?.isChecked = true
    }

    private fun setupSearch() {
        binding.editTextSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString()
                lifecycleScope.launch {
                    if (query.isEmpty()) {
                        observeMovies()
                    } else {
                        application.repository.searchMovies(query).collectLatest { movies ->
                            movieAdapter.submitList(movies)
                        }
                    }
                }
            }
        })
    }

    private fun setupFab() {
        binding.fabAddMovie.setOnClickListener {
            val intent = Intent(this, AddEditMovieActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupThemeToggle() {
        binding.buttonThemeToggle.setOnClickListener {
            val currentNightMode = resources.configuration.uiMode and 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK
            val newNightMode = if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.MODE_NIGHT_NO
            } else {
                AppCompatDelegate.MODE_NIGHT_YES
            }
            AppCompatDelegate.setDefaultNightMode(newNightMode)
        }
    }

    private fun observeMovies() {
        lifecycleScope.launch {
            application.repository.getMoviesByGenre(currentGenre).collectLatest { movies ->
                movieAdapter.submitList(movies)
            }
        }
    }

    private fun openEditScreen(movie: Movie) {
        val intent = Intent(this, AddEditMovieActivity::class.java).apply {
            putExtra("movie_id", movie.id)
            putExtra("movie_title", movie.title)
            putExtra("movie_year", movie.year)
            putExtra("movie_description", movie.description)
            putExtra("movie_genre", movie.genre)
            putExtra("movie_rating", movie.rating)
            putExtra("movie_poster", movie.posterPath)
        }
        startActivity(intent)
    }

    private fun deleteMovie(movie: Movie) {
        lifecycleScope.launch {
            application.repository.delete(movie)
            Toast.makeText(this@MainActivity, "Фильм удален", Toast.LENGTH_SHORT).show()
        }
    }
}

class MovieAdapter(
    private val onItemClick: (Movie) -> Unit,
    private val onEditClick: (Movie) -> Unit,
    private val onDeleteClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagePoster: ImageView = itemView.findViewById(R.id.imagePoster)
        private val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        private val textYear: TextView = itemView.findViewById(R.id.textYear)
        private val textDescription: TextView = itemView.findViewById(R.id.textDescription)
        private val textGenre: TextView = itemView.findViewById(R.id.textGenre)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val buttonMore: ImageView = itemView.findViewById(R.id.buttonMore)

        fun bind(movie: Movie) {
            textTitle.text = movie.title
            textYear.text = movie.year.toString()
            textDescription.text = movie.description
            textGenre.text = movie.genre
            ratingBar.rating = movie.rating.toFloat()

            // Загрузка постера
            if (!movie.posterPath.isNullOrEmpty()) {
                val uri = Uri.parse(movie.posterPath)
                imagePoster.load(uri) {
                    transformations(RoundedCornersTransformation(16f))
                    crossfade(true)
                }
            } else {
                imagePoster.setImageResource(R.drawable.ic_movie_placeholder)
            }

            // Обработка клика по меню
            buttonMore.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.movie_menu, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onEditClick(movie)
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteClick(movie)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }

            itemView.setOnClickListener {
                onItemClick(movie)
            }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}

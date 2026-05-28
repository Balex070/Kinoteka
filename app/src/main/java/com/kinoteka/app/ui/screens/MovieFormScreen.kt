package com.kinoteka.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kinoteka.app.data.model.Movie
import com.kinoteka.app.ui.theme.*
import java.io.File
import java.io.FileOutputStream

val GENRES = listOf(
    "Комедии", "Мультфильмы", "Ужасы", "Фантастика", "Триллеры", 
    "Боевики", "Мелодрамы", "Детективы", "Фэнтези", "Аниме", 
    "Документальные", "Детские", "Биографии", "На реальных событиях", "Ток-шоу"
)

@Composable
fun MovieFormScreen(
    movie: Movie? = null,
    onSaveClick: (Movie) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(movie?.title ?: "") }
    var year by remember { mutableStateOf(movie?.year?.toString() ?: "") }
    var description by remember { mutableStateOf(movie?.description ?: "") }
    var selectedGenre by remember { mutableStateOf(movie?.genre ?: GENRES.first()) }
    var rating by remember { mutableStateOf(movie?.rating?.toString() ?: "5") }
    var posterUri by remember { mutableStateOf<String?>(movie?.posterPath) }
    
    var titleError by remember { mutableStateOf(false) }
    var yearError by remember { mutableStateOf(false) }
    var ratingError by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Save image to internal storage
            val imagePath = context.contentResolver.openInputStream(it)?.use { inputStream ->
                val fileName = "poster_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file.absolutePath
            }
            posterUri = imagePath
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (movie == null) "Добавить фильм" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Validate inputs
                            titleError = title.isBlank()
                            yearError = year.isBlank() || year.toIntOrNull() == null || year.toInt() !in 1900..2100
                            ratingError = rating.isBlank() || rating.toIntOrNull() == null || rating.toInt() !in 1..10
                            
                            if (!titleError && !yearError && !ratingError) {
                                val newMovie = Movie(
                                    id = movie?.id ?: 0,
                                    title = title.trim(),
                                    year = year.toInt(),
                                    description = description.trim(),
                                    posterPath = posterUri,
                                    genre = selectedGenre,
                                    rating = rating.toInt()
                                )
                                onSaveClick(newMovie)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Poster Selection
            Box(
                modifier = Modifier
                    .size(200.dp, 280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Gray.copy(alpha = 0.3f),
                                Color.DarkGray.copy(alpha = 0.5f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (posterUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(posterUri!!))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Poster",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📷",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Нажмите для выбора постера",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = titleError,
                supportingText = if (titleError) {{ Text("Введите название") }} else null,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Year Field
            OutlinedTextField(
                value = year,
                onValueChange = { year = it.filter { char -> char.isDigit() } },
                label = { Text("Год *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                isError = yearError,
                supportingText = if (yearError) {{ Text("Введите год от 1900 до 2100") }} else null,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Genre Dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedGenre,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Жанр") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    GENRES.forEach { genre ->
                        DropdownMenuItem(
                            text = { Text(genre) },
                            onClick = {
                                selectedGenre = genre
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rating Field
            OutlinedTextField(
                value = rating,
                onValueChange = { 
                    if (it.isEmpty() || it.toIntOrNull() in 1..10) {
                        rating = it 
                    }
                },
                label = { Text("Оценка (1-10) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                isError = ratingError,
                supportingText = if (ratingError) {{ Text("Введите оценку от 1 до 10") }} else null,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Rating Slider Visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..10) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (i <= (rating.toIntOrNull() ?: 0)) {
                                    when {
                                        i >= 7 -> RatingGood
                                        i >= 4 -> RatingMedium
                                        else -> RatingBad
                                    }
                                } else {
                                    Color.Gray.copy(alpha = 0.3f)
                                }
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

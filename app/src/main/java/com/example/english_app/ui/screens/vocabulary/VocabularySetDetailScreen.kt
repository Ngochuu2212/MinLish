package com.example.english_app.ui.screens.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.english_app.data.local.entity.WordEntity
import com.example.english_app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularySetDetailScreen(
    setId: Int,
    viewModel: VocabularyViewModel,
    onNavigateToAddWord: () -> Unit,
    onNavigateToEditWord: (Int) -> Unit,
    onNavigateToFlashcard: () -> Unit,
    onNavigateToEditSet: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteWord by remember { mutableStateOf<WordEntity?>(null) }
    LaunchedEffect(setId) { viewModel.loadSet(setId) }

    val set = uiState.selectedSet

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEditSet) {
                        Icon(Icons.Default.Edit, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onNavigateToFlashcard,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    enabled = uiState.words.isNotEmpty()
                ) {
                    Text("Start learning ", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("▶", fontSize = 14.sp)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddWord,
                containerColor = NavyPrimary,
                contentColor = SurfaceWhite,
                modifier = Modifier.padding(bottom = 72.dp)
            ) { Icon(Icons.Default.Add, "Add Word") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header info
            item {
                set?.let {
                    Text(it.name, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = TextPrimary, modifier = Modifier.padding(bottom = 10.dp))

                    // Tags
                    if (it.tags.isNotBlank()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 14.dp)) {
                            it.tags.split(",").forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(CardBg)
                                        .border(1.dp, Color(0xFFDAE3F7), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("#${tag.trim()}", fontSize = 12.sp, color = NavyPrimary,
                                        fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Stats row
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.padding(bottom = 20.dp)) {
                        Column {
                            Text("TOTAL WORDS", fontSize = 10.sp, color = TextSecondary,
                                letterSpacing = 0.5.sp)
                            Text("${uiState.words.size} Words", fontSize = 15.sp,
                                fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("DIFFICULTY", fontSize = 10.sp, color = TextSecondary,
                                letterSpacing = 0.5.sp)
                            val wordCount = uiState.words.size
                            val (difficultyLabel, difficultyColor) = when {
                                wordCount <= 10 -> "Easy" to Color(0xFF4CAF50)
                                wordCount <= 30 -> "Medium" to Color(0xFFFF9800)
                                else -> "Hard" to Color(0xFFF44336)
                            }
                            Text(difficultyLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                color = difficultyColor)
                        }
                    }

                    // Section header
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("VOCABULARY LIST", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = TextSecondary, letterSpacing = 1.sp)
                        TextButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {
                            Text("Show all", fontSize = 13.sp, color = NavyPrimary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (uiState.words.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 60.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📝", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("No words yet. Tap + to add your first word.",
                                color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(uiState.words) { word ->
                    VocabWordRow(
                        word = word,
                        onEdit = { onNavigateToEditWord(word.id) },
                        onDelete = { deleteWord = word }
                    )
                    Spacer(Modifier.height(1.dp))
                }
            }
        }
    }

    deleteWord?.let { word ->
        AlertDialog(
            onDismissRequest = { deleteWord = null },
            title = { Text("Delete Word") },
            text = { Text("Delete '${word.word}'?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteWord(word); deleteWord = null }) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = { TextButton(onClick = { deleteWord = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun VocabWordRow(word: WordEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    var bookmarked by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFFF0F0F0), RoundedCornerShape(0.dp))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(word.word, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = TextPrimary)
                        if (word.pronunciation.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Text("[${word.pronunciation}]", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Text(word.meaning, fontSize = 13.sp, color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp))
                }
                IconButton(
                    onClick = { bookmarked = !bookmarked },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (bookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        null,
                        tint = if (bookmarked) NavyPrimary else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (expanded) {
                HorizontalDivider(color = Color(0xFFF5F5F5))
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .background(BgLight)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (word.example.isNotBlank()) WordDetailItem("Example", word.example)
                    if (word.description.isNotBlank()) WordDetailItem("Description", word.description)
                    if (word.collocation.isNotBlank()) WordDetailItem("Collocation", word.collocation)
                    if (word.relatedWords.isNotBlank()) WordDetailItem("Related", word.relatedWords)
                    if (word.note.isNotBlank()) WordDetailItem("Note", word.note)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)) {
                        OutlinedButton(onClick = onEdit,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            shape = RoundedCornerShape(8.dp)) {
                            Text("Edit", fontSize = 12.sp, color = NavyPrimary)
                        }
                        OutlinedButton(onClick = onDelete,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)) {
                            Text("Delete", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordDetailItem(label: String, value: String) {
    Row {
        Text("$label: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
        Text(value, fontSize = 12.sp, color = TextPrimary)
    }
}


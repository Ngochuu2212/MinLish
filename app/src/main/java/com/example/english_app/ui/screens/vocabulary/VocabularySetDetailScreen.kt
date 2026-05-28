package com.example.english_app.ui.screens.vocabulary

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val context = LocalContext.current
    var deleteWord by remember { mutableStateOf<WordEntity?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(setId) { viewModel.loadSet(setId) }

    // Import file picker (CSV / plain text / any)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importFromCsv(context, it, setId) }
    }

    // Show result snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.importedCount) {
        uiState.importedCount?.let { count ->
            snackbarHostState.showSnackbar("✅ Imported $count words successfully!")
            viewModel.clearImportResult()
        }
    }
    LaunchedEffect(uiState.importError) {
        uiState.importError?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearImportResult()
        }
    }

    val set = uiState.selectedSet

    Scaffold(
        containerColor = BgLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    // Edit set button
                    IconButton(onClick = onNavigateToEditSet) {
                        Icon(Icons.Default.Edit, null, tint = TextPrimary)
                    }
                    // More menu (Import / Export)
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = TextPrimary)
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("📥  Import CSV") },
                                onClick = { showMoreMenu = false; showImportDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("📤  Export CSV") },
                                onClick = { showMoreMenu = false; viewModel.exportToCsv(context, setId) }
                            )
                        }
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
        // Loading overlay for import
        if (uiState.isImporting) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ...existing code...
            item {
                set?.let {
                    Text(it.name, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = TextPrimary, modifier = Modifier.padding(bottom = 10.dp))

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

    // Delete word dialog
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

    // Import CSV dialog
    if (showImportDialog) {
        ImportCsvDialog(
            onDismiss = { showImportDialog = false },
            onDownloadTemplate = { viewModel.downloadTemplate(context) },
            onPickFile = {
                showImportDialog = false
                importLauncher.launch("*/*")
            }
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

// ─────────────────────── Import CSV Dialog ────────────────────────────────────
@Composable
fun ImportCsvDialog(
    onDismiss: () -> Unit,
    onDownloadTemplate: () -> Unit,
    onPickFile: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Text("📥  Import CSV", fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text("Import words from a CSV file into this set.",
                    fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Format guide
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("CSV Columns (in order):", fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        listOf(
                            "1. word ✳ required",
                            "2. pronunciation",
                            "3. meaning ✳ required",
                            "4. description",
                            "5. example",
                            "6. collocation",
                            "7. relatedWords",
                            "8. note"
                        ).forEach { col ->
                            Text(col, fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
                // Download template button
                OutlinedButton(
                    onClick = onDownloadTemplate,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                ) {
                    Icon(Icons.Default.FileDownload, null,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    Text("Download Template (.csv)", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPickFile,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.FileUpload, null,
                    modifier = Modifier.size(16.dp).padding(end = 4.dp))
                Text("Choose CSV File")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}


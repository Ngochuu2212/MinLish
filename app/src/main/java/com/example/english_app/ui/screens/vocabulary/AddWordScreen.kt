package com.example.english_app.ui.screens.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    setId: Int,
    viewModel: VocabularyViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var word by remember { mutableStateOf("") }
    var pronunciation by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var collocation by remember { mutableStateOf("") }
    var relatedWords by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Word") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Word Information", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

            OutlinedTextField(value = word, onValueChange = { word = it },
                label = { Text("Word *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = pronunciation, onValueChange = { pronunciation = it },
                label = { Text("Pronunciation (e.g. /ˈwɜːrd/)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = meaning, onValueChange = { meaning = it },
                label = { Text("Meaning *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = uiState.error?.contains("meaning") == true)

            HorizontalDivider()
            Text("Additional Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

            OutlinedTextField(value = description, onValueChange = { description = it },
                label = { Text("Description (English)") },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
            OutlinedTextField(value = example, onValueChange = { example = it },
                label = { Text("Example Sentence") },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3)
            OutlinedTextField(value = collocation, onValueChange = { collocation = it },
                label = { Text("Collocation") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = relatedWords, onValueChange = { relatedWords = it },
                label = { Text("Related Words") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = note, onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3)

            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.addWord(setId, word, pronunciation, meaning, description,
                        example, collocation, relatedWords, note, onSuccess)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Add Word") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWordScreen(
    wordId: Int,
    viewModel: VocabularyViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(wordId) { viewModel.loadWord(wordId) }

    val original = uiState.selectedWord
    var word by remember(original) { mutableStateOf(original?.word ?: "") }
    var pronunciation by remember(original) { mutableStateOf(original?.pronunciation ?: "") }
    var meaning by remember(original) { mutableStateOf(original?.meaning ?: "") }
    var description by remember(original) { mutableStateOf(original?.description ?: "") }
    var example by remember(original) { mutableStateOf(original?.example ?: "") }
    var collocation by remember(original) { mutableStateOf(original?.collocation ?: "") }
    var relatedWords by remember(original) { mutableStateOf(original?.relatedWords ?: "") }
    var note by remember(original) { mutableStateOf(original?.note ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Word") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (original == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(value = word, onValueChange = { word = it },
                label = { Text("Word *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = pronunciation, onValueChange = { pronunciation = it },
                label = { Text("Pronunciation") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = meaning, onValueChange = { meaning = it },
                label = { Text("Meaning *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = description, onValueChange = { description = it },
                label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
            OutlinedTextField(value = example, onValueChange = { example = it },
                label = { Text("Example") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3)
            OutlinedTextField(value = collocation, onValueChange = { collocation = it },
                label = { Text("Collocation") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = relatedWords, onValueChange = { relatedWords = it },
                label = { Text("Related Words") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = note, onValueChange = { note = it },
                label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3)

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.updateWord(
                        original.copy(word = word, pronunciation = pronunciation, meaning = meaning,
                            description = description, example = example, collocation = collocation,
                            relatedWords = relatedWords, note = note),
                        onSuccess
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Save Changes") }
        }
    }
}


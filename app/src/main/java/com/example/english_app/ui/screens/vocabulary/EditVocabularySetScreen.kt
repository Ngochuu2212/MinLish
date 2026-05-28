package com.example.english_app.ui.screens.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.english_app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVocabularySetScreen(
    setId: Int,
    viewModel: VocabularyViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load set data
    LaunchedEffect(setId) { viewModel.loadSet(setId) }

    val set = uiState.selectedSet

    // Pre-fill fields when set data is loaded
    var name by remember(set) { mutableStateOf(set?.name ?: "") }
    var description by remember(set) { mutableStateOf(set?.description ?: "") }
    var tags by remember(set) { mutableStateOf(set?.tags ?: "") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color(0xFFE5E7EB),
        focusedBorderColor = NavyPrimary
    )

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Set",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        if (set == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = NavyPrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Set Name
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Set Name *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; viewModel.clearError() },
                    placeholder = { Text("e.g. IELTS Vocabulary", color = TextSecondary) },
                    singleLine = true,
                    isError = uiState.error != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
            }

            // Description
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Description",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Short description (optional)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
            }

            // Tags
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Tags",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    placeholder = { Text("e.g. IELTS, Business, Travel", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
                Text(
                    "Separate tags with commas",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            if (uiState.error != null) {
                Text(
                    uiState.error!!,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    viewModel.updateSet(set, name, description, tags) { onSuccess() }
                },
                enabled = !uiState.actionSuccess,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


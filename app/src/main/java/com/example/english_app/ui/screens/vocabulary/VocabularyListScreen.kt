package com.example.english_app.ui.screens.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.english_app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyListScreen(
    viewModel: VocabularyViewModel,
    onNavigateToSet: (Int) -> Unit,
    onNavigateToAddSet: () -> Unit,
    onNavigateToEditSet: (Int) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteDialog by remember { mutableStateOf<VocabSetWithCount?>(null) }
    var menuExpandedFor by remember { mutableStateOf<Int?>(null) } // setId của item đang mở menu

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = { Text("My Sets", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddSet,
                containerColor = NavyPrimary, contentColor = SurfaceWhite,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Default.Add, "Add Set") }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
            return@Scaffold
        }

        if (uiState.sets.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📚", fontSize = 64.sp)
                    Text("No vocabulary sets yet", fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp, color = TextPrimary)
                    Text("Create your first set to get started", fontSize = 13.sp, color = TextSecondary)
                    Button(onClick = onNavigateToAddSet,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) {
                        Text("Create Set")
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("${uiState.sets.size} sets", fontSize = 13.sp, color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp))
            }
            items(uiState.sets) { item ->
                Card(
                    onClick = { onNavigateToSet(item.set.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(CardBg),
                            contentAlignment = Alignment.Center
                        ) { Text("📖", fontSize = 22.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.set.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                                color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${item.wordCount} words", fontSize = 12.sp, color = NavyPrimary,
                                    fontWeight = FontWeight.Medium)
                                if (item.set.tags.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CardBg)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(item.set.tags.split(",").first().trim(),
                                            fontSize = 10.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                        Box {
                            IconButton(onClick = { menuExpandedFor = item.set.id }) {
                                Icon(Icons.Default.MoreVert, null, tint = TextSecondary)
                            }
                            DropdownMenu(
                                expanded = menuExpandedFor == item.set.id,
                                onDismissRequest = { menuExpandedFor = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = NavyPrimary) },
                                    onClick = {
                                        menuExpandedFor = null
                                        onNavigateToEditSet(item.set.id)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = ErrorRed) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRed) },
                                    onClick = {
                                        menuExpandedFor = null
                                        deleteDialog = item
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    deleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteDialog = null },
            title = { Text("Delete '${item.set.name}'?") },
            text = { Text("This will delete all ${item.wordCount} words in this set.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSet(item.set); deleteDialog = null }) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = { TextButton(onClick = { deleteDialog = null }) { Text("Cancel") } }
        )
    }
}

package com.example.english_app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.english_app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user

    // Auto-clear "Profile saved" message after 3 seconds
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            delay(3000)
            viewModel.clearSuccess()
        }
    }

    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var goal by remember(user) { mutableStateOf(user?.learningGoal ?: "General") }
    var level by remember(user) { mutableStateOf(user?.level ?: "A1") }
    var dailyCount by remember(user) { mutableStateOf(user?.dailyWordCount?.toString() ?: "10") }
    var levelExpanded by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }
    val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
    val goals  = listOf("General", "IELTS", "TOEIC", "Business", "Travel", "Conversation")

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = NavyPrimary
    )

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar block
            Column(
                modifier = Modifier.fillMaxWidth().background(SurfaceWhite)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(CardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user?.name?.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 32.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
                Spacer(Modifier.height(10.dp))
                Text(user?.name ?: "", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(user?.email ?: "", fontSize = 13.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(12.dp))

            // Settings card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Personal Settings", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = TextSecondary)

                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Display Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp), colors = fieldColors
                    )

                    ExposedDropdownMenuBox(expanded = goalExpanded, onExpandedChange = { goalExpanded = it }) {
                        OutlinedTextField(
                            value = goal, onValueChange = {}, readOnly = true,
                            label = { Text("Learning Goal") },
                            leadingIcon = { Icon(Icons.Default.TrackChanges, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), colors = fieldColors
                        )
                        ExposedDropdownMenu(expanded = goalExpanded, onDismissRequest = { goalExpanded = false }) {
                            goals.forEach { g ->
                                DropdownMenuItem(text = { Text(g) }, onClick = { goal = g; goalExpanded = false })
                            }
                        }
                    }

                    ExposedDropdownMenuBox(expanded = levelExpanded, onExpandedChange = { levelExpanded = it }) {
                        OutlinedTextField(
                            value = level, onValueChange = {}, readOnly = true,
                            label = { Text("Current Level") },
                            leadingIcon = { Icon(Icons.Default.Grade, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), colors = fieldColors
                        )
                        ExposedDropdownMenu(expanded = levelExpanded, onDismissRequest = { levelExpanded = false }) {
                            levels.forEach { l ->
                                DropdownMenuItem(text = { Text(l) }, onClick = { level = l; levelExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dailyCount,
                        onValueChange = { dailyCount = it.filter { c -> c.isDigit() } },
                        label = { Text("Daily Word Goal") },
                        leadingIcon = { Icon(Icons.Default.Today, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp), colors = fieldColors,
                        suffix = { Text("words/day", color = TextSecondary, fontSize = 13.sp) }
                    )

                    if (uiState.saveSuccess) {
                        Text("✓ Profile saved!", color = SuccessGreen, fontSize = 13.sp,
                            fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { viewModel.updateProfile(name, goal, level, dailyCount.toIntOrNull() ?: 10) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = SurfaceWhite, strokeWidth = 2.dp)
                        else Text("Save Changes", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Sign out button
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                TextButton(
                    onClick = { onLogout() },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Logout, null, tint = ErrorRed,
                        modifier = Modifier.size(20.dp).padding(end = 8.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", color = ErrorRed, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

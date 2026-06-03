package com.example.english_app.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    val context = LocalContext.current

    // Auto-clear "Profile saved" message after 3 seconds
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            delay(3000)
            viewModel.clearSuccess()
        }
    }

    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var goal by remember(user) { mutableStateOf(user?.learningGoal ?: "General") }
    var dailyCount by remember(user) { mutableStateOf(user?.dailyWordCount?.toString() ?: "10") }
    var goalExpanded by remember { mutableStateOf(false) }
    val goals  = listOf("General", "IELTS", "TOEIC", "Business", "Travel", "Conversation")

    // Image picker launcher
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.saveAvatar(context, it) }
    }

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
                // Avatar container với nút camera overlay
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(CardBg)
                            .border(2.dp, NavyPrimary.copy(alpha = 0.15f), CircleShape)
                            .clickable {
                                avatarPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.avatarUri?.isNotBlank() == true) {
                            // Hiển thị ảnh đã upload
                            AsyncImage(
                                model = user.avatarUri,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Hiển thị chữ cái đầu tên nếu chưa có ảnh
                            Text(
                                user?.name?.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 32.sp, fontWeight = FontWeight.Bold, color = NavyPrimary
                            )
                        }
                    }
                    // Nút camera nhỏ góc dưới-phải
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary)
                            .border(2.dp, SurfaceWhite, CircleShape)
                            .clickable {
                                avatarPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt, contentDescription = "Đổi ảnh",
                            tint = Color.White, modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(user?.name ?: "", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(user?.email ?: "", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Nhấn vào ảnh để thay đổi",
                    fontSize = 11.sp, color = TextSecondary.copy(alpha = 0.7f)
                )
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

                    // Current Level — chỉ đọc, tính tự động từ số từ đã học
                    OutlinedTextField(
                        value = uiState.computedLevel,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Current Level") },
                        leadingIcon = { Icon(Icons.Default.Grade, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color(0xFFE5E7EB),
                            disabledLabelColor = TextSecondary,
                            disabledTextColor = TextPrimary,
                            disabledLeadingIconColor = TextSecondary
                        ),
                        suffix = { Text("auto", color = NavyPrimary.copy(alpha = 0.6f), fontSize = 11.sp) }
                    )

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
                    if (uiState.error != null) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { viewModel.updateProfile(name, goal, dailyCount.toIntOrNull() ?: 10) },
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

package com.example.english_app.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.english_app.data.repository.ProgressStats
import com.example.english_app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProgress() }

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = { Text("Your Progress", fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, null, tint = TextPrimary)
                    }
                },
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
                .background(BgLight)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Level Badge + Word Count ──────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    Text(uiState.level.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF388E3C), letterSpacing = 0.5.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text("${uiState.stats.learnedWords}", fontSize = 56.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary)
                Text("words learned", fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(12.dp))

            // ── Streak Banner ─────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${uiState.stats.streak} day streak", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Keep it up, you're on fire!", fontSize = 12.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Weekly Activity ───────────────────────────────
            Text("Weekly Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Spacer(Modifier.height(2.dp))
            Text("This Week", fontSize = 12.sp, color = NavyPrimary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textAlign = TextAlign.End)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                WeeklyBarChart(
                    activity = uiState.recentActivity,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Accuracy + Best Score ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Accuracy circle
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularAccuracyIndicator(uiState.stats.accuracy)
                        Spacer(Modifier.height(8.dp))
                        Text("Accuracy", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = TextPrimary)
                        val label = when {
                            uiState.stats.accuracy >= 0.9f -> "Excellent precision"
                            uiState.stats.accuracy >= 0.7f -> "Good job!"
                            else -> "Keep practicing"
                        }
                        Text(label, fontSize = 11.sp, color = TextSecondary)
                    }
                }

                // Best Score
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Best Score", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = TextPrimary)
                        Text("${uiState.stats.totalReviews * 10} Points", fontSize = 11.sp,
                            color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Achievements ──────────────────────────────────
            Text("Achievements", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AchievementBadge("⚡", uiState.stats.streak >= 3)
                AchievementBadge("📚", uiState.stats.learnedWords >= 10)
                AchievementBadge("🏅", uiState.stats.totalReviews >= 50)
                AchievementBadge("🎯", uiState.stats.accuracy >= 0.8f)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun CircularAccuracyIndicator(accuracy: Float) {
    val pct = (accuracy * 100).toInt()
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
        CircularProgressIndicator(
            progress = { accuracy },
            modifier = Modifier.size(80.dp),
            color = NavyPrimary,
            trackColor = CardBg,
            strokeWidth = 8.dp,
            strokeCap = StrokeCap.Round
        )
        Text("$pct%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
fun WeeklyBarChart(activity: Map<String, Int>, modifier: Modifier = Modifier) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val values = activity.values.toList().takeLast(7).let { list ->
        if (list.size < 7) List(7 - list.size) { 0 } + list else list
    }
    val maxVal = (values.maxOrNull() ?: 1).coerceAtLeast(1)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEachIndexed { idx, day ->
                val v = values.getOrElse(idx) { 0 }
                val isToday = idx == 3 // highlight Thursday like Figma
                val heightFraction = if (maxVal > 0) v.toFloat() / maxVal else 0f
                val minH = 4.dp
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.width(32.dp).fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight(heightFraction.coerceAtLeast(0.04f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (isToday) NavyPrimary else CardBg)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            days.forEach { day ->
                Text(day, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun AchievementBadge(emoji: String, unlocked: Boolean) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (unlocked) CardBg else Color(0xFFF5F5F5))
            .border(1.dp, if (unlocked) Color(0xFFDAE3F7) else Color(0xFFEEEEEE), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 26.sp,
            color = if (unlocked) Color.Unspecified else TextSecondary)
    }
}


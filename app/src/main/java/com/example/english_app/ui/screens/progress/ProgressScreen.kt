package com.example.english_app.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.english_app.data.repository.ProgressStats
import com.example.english_app.ui.theme.*
import java.util.Calendar

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

            // ── Retention Rate Chart ──────────────────────────
            Text("Retention Rate", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Spacer(Modifier.height(2.dp))
            Text("Last 7 Days", fontSize = 12.sp, color = NavyPrimary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textAlign = TextAlign.End)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                RetentionRateChart(
                    retentionByDay = uiState.retentionByDay,
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
                AchievementBadge(
                    emoji = "⚡",
                    unlocked = uiState.stats.streak >= 3,
                    title = "On Fire!",
                    description = "Maintain a 3-day learning streak.",
                    progress = "Current streak: ${uiState.stats.streak} day(s)"
                )
                AchievementBadge(
                    emoji = "📚",
                    unlocked = uiState.stats.learnedWords >= 10,
                    title = "Bookworm",
                    description = "Learn at least 10 vocabulary words.",
                    progress = "Words learned: ${uiState.stats.learnedWords}"
                )
                AchievementBadge(
                    emoji = "🏅",
                    unlocked = uiState.stats.totalReviews >= 50,
                    title = "Dedicated",
                    description = "Complete 50 review sessions.",
                    progress = "Total reviews: ${uiState.stats.totalReviews}"
                )
                AchievementBadge(
                    emoji = "🎯",
                    unlocked = uiState.stats.accuracy >= 0.8f,
                    title = "Sharp Mind",
                    description = "Achieve 80% accuracy or above.",
                    progress = "Accuracy: ${(uiState.stats.accuracy * 100).toInt()}%"
                )
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

    // Tính index ngày hôm nay trong tuần (Mon=0 … Sun=6)
    val todayIdx = run {
        val cal = Calendar.getInstance()
        // Calendar.DAY_OF_WEEK: Sun=1, Mon=2, …, Sat=7 → chuyển về Mon=0..Sun=6
        (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEachIndexed { idx, day ->
                val v = values.getOrElse(idx) { 0 }
                val isToday = idx == todayIdx
                val heightFraction = if (maxVal > 0) v.toFloat() / maxVal else 0f
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
            days.forEachIndexed { idx, day ->
                Text(
                    day, fontSize = 10.sp,
                    color = if (idx == todayIdx) NavyPrimary else TextSecondary,
                    fontWeight = if (idx == todayIdx) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AchievementBadge(
    emoji: String,
    unlocked: Boolean,
    title: String,
    description: String,
    progress: String
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (unlocked) CardBg else Color(0xFFF5F5F5))
            .border(1.dp, if (unlocked) Color(0xFFDAE3F7) else Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
            .clickable { showDialog = true },
        contentAlignment = Alignment.Center
    ) {
        Text(
            emoji, fontSize = 26.sp,
            color = if (unlocked) Color.Unspecified else TextSecondary
        )
        // Lock overlay nếu chưa mở khóa
        if (!unlocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x55FFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text("🔒", fontSize = 10.sp, modifier = Modifier.padding(3.dp))
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Text(emoji, fontSize = 36.sp)
            },
            title = {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (unlocked) Color(0xFFE8F5E9) else Color(0xFFF5F5F5))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (unlocked) "✅" else "🔒",
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (unlocked) "Unlocked! $progress"
                                else "Locked — $progress",
                                fontSize = 13.sp,
                                color = if (unlocked) Color(0xFF388E3C) else TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Got it", color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun RetentionRateChart(
    retentionByDay: List<Float?>,
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Tính index ngày hôm nay (Mon=0…Sun=6)
    val todayIdx = run {
        val cal = java.util.Calendar.getInstance()
        (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
    }

    // Đảm bảo data đủ 7 phần tử
    val data: List<Float?> = if (retentionByDay.size == 7) retentionByDay
    else List(7) { retentionByDay.getOrNull(it) }

    val lineColor = NavyPrimary
    val fillColor = NavyPrimary.copy(alpha = 0.12f)
    val dotColor = NavyPrimary
    val emptyDotColor = Color(0xFFDAE3F7)
    val gridColor = Color(0xFFF0F0F0)
    val todayColor = Color(0xFFFF6B35)

    Column(modifier = modifier) {
        // Y-axis labels + Chart area
        Row(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            // Y-axis labels
            Column(
                modifier = Modifier.width(32.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("100%", "50%", "0%").forEach { label ->
                    Text(label, fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth())
                }
            }

            Spacer(Modifier.width(4.dp))

            // Chart canvas
            Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val w = size.width
                val h = size.height
                val stepX = w / 6f  // 7 points → 6 gaps

                // Grid lines at 0%, 50%, 100%
                listOf(0f, 0.5f, 1f).forEach { pct ->
                    val y = h - pct * h
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Collect points with data
                val points = data.mapIndexedNotNull { idx, value ->
                    if (value != null) {
                        val x = idx * stepX
                        val y = h - value.coerceIn(0f, 1f) * h
                        Pair(idx, androidx.compose.ui.geometry.Offset(x, y))
                    } else null
                }

                if (points.size >= 2) {
                    // Filled area under the line
                    val path = Path().apply {
                        moveTo(points.first().second.x, h)
                        points.forEach { (_, pt) -> lineTo(pt.x, pt.y) }
                        lineTo(points.last().second.x, h)
                        close()
                    }
                    drawPath(path, color = fillColor)

                    // Line connecting points
                    val linePath = Path().apply {
                        moveTo(points.first().second.x, points.first().second.y)
                        points.drop(1).forEach { (_, pt) -> lineTo(pt.x, pt.y) }
                    }
                    drawPath(
                        linePath,
                        color = lineColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Dots for all days
                data.forEachIndexed { idx, value ->
                    val x = idx * stepX
                    val isToday = idx == todayIdx

                    if (value != null) {
                        val y = h - value.coerceIn(0f, 1f) * h
                        val dotC = if (isToday) todayColor else dotColor
                        // Outer white ring
                        drawCircle(color = androidx.compose.ui.graphics.Color.White,
                            radius = 5.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y))
                        // Filled dot
                        drawCircle(color = dotC,
                            radius = 4.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y))
                    } else {
                        // Empty dot for no-data day
                        val y = h  // at bottom
                        drawCircle(
                            color = emptyDotColor,
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // X-axis day labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { idx, day ->
                Text(
                    day, fontSize = 10.sp,
                    color = when (idx) {
                        todayIdx -> NavyPrimary
                        else -> TextSecondary
                    },
                    fontWeight = if (idx == todayIdx) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(dotColor))
                Spacer(Modifier.width(4.dp))
                Text("Retention Rate", fontSize = 10.sp, color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(todayColor))
                Spacer(Modifier.width(4.dp))
                Text("Today", fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}

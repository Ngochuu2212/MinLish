package com.example.english_app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.english_app.data.local.entity.VocabularySetEntity
import com.example.english_app.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToLearn: () -> Unit,
    onNavigateToDailyReview: () -> Unit,
    onNavigateToAllSets: () -> Unit,
    onNavigateToSet: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadData() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Header ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text("Hi, ${uiState.userName} 👋",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Ready to expand your vocabulary?",
                    fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardBg)
                    .clickable {},
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Notifications, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Stats Card ───────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.5.dp, Color(0xFFDAE3F7), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // New Words
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(CardBg),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Add, null, tint = NavyPrimary, modifier = Modifier.size(18.dp)) }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("NEW WORDS", fontSize = 10.sp, fontWeight = FontWeight.Medium,
                                color = TextSecondary, letterSpacing = 0.5.sp)
                            Text("${uiState.newWords}", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                color = TextPrimary)
                        }
                    }
                    // Streak badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 16.sp)
                        Spacer(Modifier.width(4.dp))
                        Text("${uiState.streak} days", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = OrangeDark)
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(Modifier.height(12.dp))

                // To Review
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(CardBg),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Refresh, null, tint = NavyPrimary, modifier = Modifier.size(18.dp)) }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("TO REVIEW", fontSize = 10.sp, fontWeight = FontWeight.Medium,
                            color = TextSecondary, letterSpacing = 0.5.sp)
                        Text("${uiState.dueWords}", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            color = TextPrimary)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Mastery Progress
                val masteryPct = (uiState.masteryProgress * 100).toInt()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mastery Progress", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = TextPrimary)
                    Text("$masteryPct%", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = NavyPrimary)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { uiState.masteryProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = NavyPrimary,
                    trackColor = CardBg
                )
                Spacer(Modifier.height(6.dp))
                Text("${uiState.learnedWords} of ${uiState.totalWords.coerceAtLeast(1)} words mastered",
                    fontSize = 11.sp, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Action Buttons ────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onNavigateToLearn,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Icon(Icons.Default.School, null, Modifier.size(20.dp).padding(end = 8.dp))
                Text("  Learn new words ", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("›", fontSize = 20.sp, fontWeight = FontWeight.Light)
            }
            Button(
                onClick = onNavigateToDailyReview,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
            ) {
                Icon(Icons.Default.Layers, null, Modifier.size(20.dp).padding(end = 8.dp))
                Text("  Review due cards ", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = SurfaceWhite)
                Spacer(Modifier.weight(1f))
                Text("›", fontSize = 20.sp, fontWeight = FontWeight.Light, color = SurfaceWhite)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Recent Lists ──────────────────────────────────────
        if (uiState.recentSets.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Lists", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                TextButton(onClick = onNavigateToAllSets, contentPadding = PaddingValues(0.dp)) {
                    Text("View all", fontSize = 13.sp, color = NavyPrimary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.recentSets.forEach { set ->
                    RecentSetRow(set = set, onClick = { onNavigateToSet(set.id) })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Study Tip ─────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Study Tip", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Reviewing words just before bed helps improve memory retention!",
                        fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(CardBg),
                    contentAlignment = Alignment.Center
                ) { Text("💡", fontSize = 22.sp) }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun RecentSetRow(set: VocabularySetEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(CardBg),
                contentAlignment = Alignment.Center
            ) { Text("📚", fontSize = 20.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(set.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                if (set.description.isNotBlank()) {
                    Text(set.description, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                }
            }
            Icon(Icons.Default.MoreVert, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}


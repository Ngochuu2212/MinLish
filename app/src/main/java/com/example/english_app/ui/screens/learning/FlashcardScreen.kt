package com.example.english_app.ui.screens.learning

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.english_app.domain.srs.SM2Algorithm
import com.example.english_app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    setId: Int,
    viewModel: LearningViewModel,
    onBack: () -> Unit,
    onNavigateToEditWord: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(setId) { viewModel.startFlashcardSession(setId) }

    FlashcardSessionScaffold(
        title = uiState.sets.find { it.id == setId }?.name ?: "Vocabulary Deck",
        uiState = uiState,
        onFlip = { viewModel.flipCard() },
        onRate = { viewModel.rateWord(it) },
        onRestart = { viewModel.startFlashcardSession(setId) },
        onNavigateToEditWord = onNavigateToEditWord,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReviewScreen(
    viewModel: LearningViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.startDailyReview() }

    FlashcardSessionScaffold(
        title = "Daily Review",
        uiState = uiState,
        onFlip = { viewModel.flipCard() },
        onRate = { viewModel.rateWord(it) },
        onRestart = { viewModel.startDailyReview() },
        onNavigateToEditWord = {},
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlashcardSessionScaffold(
    title: String,
    uiState: LearningUiState,
    onFlip: () -> Unit,
    onRate: (Int) -> Unit,
    onRestart: () -> Unit,
    onNavigateToEditWord: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // ── Text-to-Speech setup ──────────────────────────────────────────────
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }
    DisposableEffect(Unit) {
        onDispose { tts.shutdown() }
    }
    val speak: (String) -> Unit = { word ->
        if (ttsReady) {
            tts.language = Locale.ENGLISH
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // ── Search: mở Cambridge Dictionary ──────────────────────────────────
    val searchWord: (String) -> Unit = { word ->
        val uri = Uri.parse("https://dictionary.cambridge.org/dictionary/english/${Uri.encode(word)}")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    Scaffold(
        containerColor = BgLight,
        topBar = {
            Column(modifier = Modifier.background(SurfaceWhite)) {
                TopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(end = 40.dp)) {
                            Text("VOCABULARY DECK", fontSize = 10.sp, color = TextSecondary,
                                letterSpacing = 0.8.sp)
                            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, null, tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                )
                // Daily goal progress bar
                if (uiState.sessionWords.isNotEmpty()) {
                    val total = uiState.sessionWords.size
                    val done  = uiState.currentIndex.coerceAtMost(total)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily Goal", fontSize = 12.sp, color = TextSecondary)
                        Text("$done/$total", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            color = NavyPrimary)
                    }
                    LinearProgressIndicator(
                        progress = { if (total > 0) done.toFloat() / total else 0f },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NavyPrimary, trackColor = CardBg
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.isSessionComplete -> SessionCompleteScreen(uiState.sessionStats, onRestart, onBack)
                uiState.sessionWords.isEmpty() -> EmptySessionScreen(onBack)
                else -> {
                    val current = uiState.sessionWords[uiState.currentIndex]
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Card ${uiState.currentIndex + 1} / ${uiState.sessionWords.size}",
                            fontSize = 13.sp, color = TextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp))

                        FlashCard(
                            word = current.word.word,
                            pronunciation = current.word.pronunciation,
                            meaning = current.word.meaning,
                            example = current.word.example,
                            isFlipped = uiState.isFlipped,
                            onClick = onFlip,
                            onSpeak = { speak(current.word.word) },
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        if (!uiState.isFlipped) {
                            Text("TAP TO REVEAL ANSWER", fontSize = 11.sp, color = TextSecondary,
                                letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 12.dp))
                        }

                        if (uiState.isFlipped) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("1M" to SM2Algorithm.QUALITY_AGAIN,
                                    "2D" to SM2Algorithm.QUALITY_HARD,
                                    "4D" to SM2Algorithm.QUALITY_GOOD,
                                    "7D" to SM2Algorithm.QUALITY_EASY
                                ).forEach { (label, _) ->
                                    Text(label, fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SrsButton(Modifier.weight(1f), "AGAIN",
                                    ErrorRed, Icons.Default.Refresh,
                                    onClick = { onRate(SM2Algorithm.QUALITY_AGAIN) })
                                SrsButton(Modifier.weight(1f), "HARD",
                                    OrangeAccent, Icons.Default.Bolt,
                                    onClick = { onRate(SM2Algorithm.QUALITY_HARD) })
                                SrsButton(Modifier.weight(1f), "GOOD",
                                    SuccessGreen, Icons.Default.Check,
                                    onClick = { onRate(SM2Algorithm.QUALITY_GOOD) },
                                    isHighlighted = true)
                                SrsButton(Modifier.weight(1f), "EASY",
                                    Color(0xFF5B8FDE), Icons.Default.SentimentVerySatisfied,
                                    onClick = { onRate(SM2Algorithm.QUALITY_EASY) })
                            }
                        } else {
                            Button(
                                onClick = onFlip,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) { Text("Reveal Answer", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Search → mở Cambridge Dictionary
                            OutlinedButton(
                                onClick = { searchWord(current.word.word) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Icon(Icons.Default.Search, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Search", fontSize = 13.sp)
                            }
                            // Edit Card → chuyển đến màn sửa từ
                            OutlinedButton(
                                onClick = { onNavigateToEditWord(current.word.id) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit Card", fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SrsButton(
    modifier: Modifier,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isHighlighted: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isHighlighted) color else SurfaceWhite,
            contentColor = if (isHighlighted) SurfaceWhite else color
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        border = if (!isHighlighted) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FlashCard(
    word: String,
    pronunciation: String,
    meaning: String,
    example: String,
    isFlipped: Boolean,
    onClick: () -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing), label = "flip"
    )

    Card(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 14f * density
        },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
            if (rotation <= 90f) {
                // Front face
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(word, fontSize = 34.sp, fontWeight = FontWeight.Bold,
                        color = TextPrimary, textAlign = TextAlign.Center)
                    if (pronunciation.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("/$pronunciation/", fontSize = 16.sp, color = TextSecondary)
                    }
                    Spacer(Modifier.height(20.dp))
                    // Nút loa → đọc từ bằng TTS
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(50))
                            .background(CardBg)
                            .border(1.dp, Color(0xFFDAE3F7), RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onSpeak, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Phát âm",
                                tint = NavyPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            } else {
                // Back face (counter-rotate)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                ) {
                    Text(word, fontSize = 18.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(Modifier.height(16.dp))
                    Text(meaning, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = NavyPrimary, textAlign = TextAlign.Center)
                    if (example.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text("\"$example\"", fontSize = 14.sp, color = TextSecondary,
                            textAlign = TextAlign.Center, fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCompleteScreen(stats: SessionStats, onRestart: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎉", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("Session Complete!", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatRow("Total Cards", "${stats.total}")
                StatRow("Correct", "${stats.correct}", SuccessGreen)
                StatRow("Again", "${stats.again}", ErrorRed)
                StatRow("Accuracy",
                    if (stats.total > 0) "${stats.correct * 100 / stats.total}%" else "N/A",
                    NavyPrimary)
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) {
            Text("Study Again", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)) {
            Text("Done", fontSize = 15.sp)
        }
    }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), color = TextSecondary, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = valueColor, fontSize = 14.sp)
    }
}

@Composable
fun EmptySessionScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎊", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("All caught up!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("No words due for review right now.", color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp), fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
            Text("Go Back")
        }
    }
}



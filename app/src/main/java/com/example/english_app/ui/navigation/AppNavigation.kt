package com.example.english_app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.english_app.ui.theme.NavyPrimary
import com.example.english_app.ui.theme.SurfaceWhite
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.english_app.MinLishApp
import com.example.english_app.ui.screens.MainBottomBar
import com.example.english_app.ui.screens.auth.AuthViewModel
import com.example.english_app.ui.screens.auth.AuthViewModelFactory
import com.example.english_app.ui.screens.auth.LoginScreen
import com.example.english_app.ui.screens.auth.RegisterScreen
import com.example.english_app.ui.screens.home.HomeScreen
import com.example.english_app.ui.screens.home.HomeViewModelFactory
import com.example.english_app.ui.screens.learning.DailyReviewScreen
import com.example.english_app.ui.screens.learning.FlashcardScreen
import com.example.english_app.ui.screens.learning.LearningViewModelFactory
import com.example.english_app.ui.screens.learning.LearnScreen
import com.example.english_app.ui.screens.progress.ProgressScreen
import com.example.english_app.ui.screens.progress.ProgressViewModelFactory
import com.example.english_app.ui.screens.profile.ProfileScreen
import com.example.english_app.ui.screens.profile.ProfileViewModelFactory
import com.example.english_app.ui.screens.vocabulary.AddVocabularySetScreen
import com.example.english_app.ui.screens.vocabulary.AddWordScreen
import com.example.english_app.ui.screens.vocabulary.EditVocabularySetScreen
import com.example.english_app.ui.screens.vocabulary.EditWordScreen
import com.example.english_app.ui.screens.vocabulary.VocabularyListScreen
import com.example.english_app.ui.screens.vocabulary.VocabularySetDetailScreen
import com.example.english_app.ui.screens.vocabulary.VocabularyViewModelFactory

// Routes that show bottom navigation bar
private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.VocabularyList.route,
    Screen.Progress.route,
    Screen.Profile.route
)

@Composable
fun AppNavigation(app: MinLishApp) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(app.authRepository))

    // Int.MIN_VALUE = đang loading (DataStore chưa emit), -1 = chưa đăng nhập, >0 = đã đăng nhập
    val userId by authViewModel.currentUserId.collectAsState(initial = Int.MIN_VALUE)

    // Đặt tất cả remember/LaunchedEffect TRƯỚC bất kỳ return nào
    var prevUserId by remember { mutableStateOf(userId) }
    LaunchedEffect(userId) {
        // Xử lý logout: khi userId từ >0 về <=0 (sau khi đã load xong)
        if (userId != Int.MIN_VALUE && prevUserId > 0 && userId <= 0) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
        prevUserId = userId
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    // Hiển thị splash screen khi đang chờ DataStore load
    if (userId == Int.MIN_VALUE) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceWhite),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📖", fontSize = 52.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "MinLish",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(Modifier.height(28.dp))
                CircularProgressIndicator(color = NavyPrimary, strokeWidth = 2.dp)
            }
        }
        return
    }

    // startDestination chỉ được tính 1 lần sau khi userId đã xác định
    val startDestination = if (userId > 0) Screen.Home.route else Screen.Login.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) MainBottomBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(viewModel = authViewModel,
                    onRegisterSuccess = {
                        authViewModel.clearSuccess()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel(factory = HomeViewModelFactory(
                        app.authRepository, app.vocabularyRepository, app.learningRepository)),
                    onNavigateToLearn = { navController.navigate(Screen.Learn.route) },
                    onNavigateToDailyReview = { navController.navigate(Screen.DailyReview.route) },
                    onNavigateToAllSets = { navController.navigate(Screen.VocabularyList.route) },
                    onNavigateToSet = { setId -> navController.navigate(Screen.VocabularySetDetail.createRoute(setId)) }
                )
            }

            composable(Screen.VocabularyList.route) {
                VocabularyListScreen(
                    viewModel = viewModel(factory = VocabularyViewModelFactory(
                        app.authRepository, app.vocabularyRepository, app.learningRepository)),
                    onNavigateToSet = { setId -> navController.navigate(Screen.VocabularySetDetail.createRoute(setId)) },
                    onNavigateToAddSet = { navController.navigate(Screen.AddVocabularySet.route) },
                    onNavigateToEditSet = { setId -> navController.navigate(Screen.EditVocabularySet.createRoute(setId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.VocabularySetDetail.route,
                arguments = listOf(navArgument("setId") { type = NavType.IntType })) { back ->
                val setId = back.arguments?.getInt("setId") ?: return@composable
                VocabularySetDetailScreen(setId = setId,
                    viewModel = viewModel(factory = VocabularyViewModelFactory(
                        app.authRepository, app.vocabularyRepository, app.learningRepository)),
                    onNavigateToAddWord = { navController.navigate(Screen.AddWord.createRoute(setId)) },
                    onNavigateToEditWord = { wId -> navController.navigate(Screen.EditWord.createRoute(wId)) },
                    onNavigateToFlashcard = { navController.navigate(Screen.Flashcard.createRoute(setId)) },
                    onNavigateToEditSet = { navController.navigate(Screen.EditVocabularySet.createRoute(setId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AddVocabularySet.route) {
                AddVocabularySetScreen(
                    viewModel = viewModel(factory = VocabularyViewModelFactory(
                        app.authRepository, app.vocabularyRepository, app.learningRepository)),
                    onSuccess = { setId ->
                        navController.navigate(Screen.VocabularySetDetail.createRoute(setId)) {
                            popUpTo(Screen.AddVocabularySet.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.EditVocabularySet.route,
                arguments = listOf(navArgument("setId") { type = NavType.IntType })) { back ->
                val setId = back.arguments?.getInt("setId") ?: return@composable
                EditVocabularySetScreen(
                    setId = setId,
                    viewModel = viewModel(factory = VocabularyViewModelFactory(
                        app.authRepository, app.vocabularyRepository, app.learningRepository)),
                    onSuccess = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AddWord.route,
                arguments = listOf(navArgument("setId") { type = NavType.IntType })) { back ->
                val setId = back.arguments?.getInt("setId") ?: return@composable
                AddWordScreen(setId = setId,
                    viewModel = viewModel(factory = VocabularyViewModelFactory(
                        app.authRepository, app.vocabularyRepository, app.learningRepository)),
                    onSuccess = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.EditWord.route,
                arguments = listOf(navArgument("wordId") { type = NavType.IntType })) { back ->
                val wordId = back.arguments?.getInt("wordId") ?: return@composable
                EditWordScreen(wordId = wordId,
                    viewModel = viewModel(factory = VocabularyViewModelFactory(
                        app.authRepository, app.vocabularyRepository, app.learningRepository)),
                    onSuccess = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Learn.route) {
                LearnScreen(
                    viewModel = viewModel(factory = LearningViewModelFactory(
                        app.authRepository, app.vocabularyRepository, app.learningRepository)),
                    onNavigateToSet = { setId -> navController.navigate(Screen.Flashcard.createRoute(setId)) },
                    onNavigateToDailyReview = { navController.navigate(Screen.DailyReview.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Flashcard.route,
                arguments = listOf(navArgument("setId") { type = NavType.IntType })) { back ->
                val setId = back.arguments?.getInt("setId") ?: return@composable
                FlashcardScreen(
                    setId = setId,
                    viewModel = viewModel(factory = LearningViewModelFactory(
                        app.authRepository, app.vocabularyRepository, app.learningRepository)),
                    onNavigateToEditWord = { wordId ->
                        navController.navigate(Screen.EditWord.createRoute(wordId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DailyReview.route) {
                DailyReviewScreen(
                    viewModel = viewModel(factory = LearningViewModelFactory(
                        app.authRepository, app.vocabularyRepository, app.learningRepository)),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Progress.route) {
                ProgressScreen(
                    viewModel = viewModel(factory = ProgressViewModelFactory(
                        app.authRepository, app.learningRepository)),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel(factory = ProfileViewModelFactory(app.authRepository)),
                    onLogout = {
                        // Chỉ gọi logout — LaunchedEffect(userId) sẽ tự navigate về Login
                        // khi DataStore emit -1. Không navigate trực tiếp ở đây để tránh double navigation.
                        authViewModel.logout()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

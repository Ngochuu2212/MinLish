package com.example.english_app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object VocabularyList : Screen("vocabulary_list")
    object VocabularySetDetail : Screen("vocabulary_set/{setId}") {
        fun createRoute(setId: Int) = "vocabulary_set/$setId"
    }
    object AddVocabularySet : Screen("add_vocabulary_set")
    object AddWord : Screen("add_word/{setId}") {
        fun createRoute(setId: Int) = "add_word/$setId"
    }
    object EditWord : Screen("edit_word/{wordId}") {
        fun createRoute(wordId: Int) = "edit_word/$wordId"
    }
    object Learn : Screen("learn")
    object Flashcard : Screen("flashcard/{setId}") {
        fun createRoute(setId: Int) = "flashcard/$setId"
    }
    object DailyReview : Screen("daily_review")
    object Progress : Screen("progress")
    object Profile : Screen("profile")
}


package com.example.english_app.data.repository

import com.example.english_app.data.local.UserPreferences
import com.example.english_app.data.local.dao.UserDao
import com.example.english_app.data.local.entity.UserEntity
import com.example.english_app.utils.PasswordUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {
    val currentUserId: Flow<Int> = userPreferences.userId

    suspend fun register(name: String, email: String, password: String): AuthResult {
        if (name.isBlank()) return AuthResult.Error("Name cannot be empty")
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return AuthResult.Error("Invalid email address")
        if (password.length < 6)
            return AuthResult.Error("Password must be at least 6 characters")

        val existing = userDao.findByEmail(email)
        if (existing != null) return AuthResult.Error("Email already registered")

        val user = UserEntity(
            name = name,
            email = email,
            passwordHash = PasswordUtils.hash(password)
        )
        val id = userDao.insert(user).toInt()
        val created = user.copy(id = id)
        return AuthResult.Success(created)
    }

    suspend fun login(email: String, password: String): AuthResult {
        if (email.isBlank()) return AuthResult.Error("Email cannot be empty")
        if (password.isBlank()) return AuthResult.Error("Password cannot be empty")

        val user = userDao.findByEmail(email) ?: return AuthResult.Error("Email not found")
        if (!PasswordUtils.verify(password, user.passwordHash))
            return AuthResult.Error("Incorrect password")

        userPreferences.saveUser(user.id, user.email, user.name)
        return AuthResult.Success(user)
    }

    suspend fun logout() {
        userPreferences.clearUser()
        FirebaseAuth.getInstance().signOut()
    }

    suspend fun loginWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return AuthResult.Error("Google sign-in failed")
            val email = firebaseUser.email ?: return AuthResult.Error("Could not get email from Google")
            val name = firebaseUser.displayName ?: email.substringBefore("@")
            val existing = userDao.findByEmail(email)
            val user = if (existing != null) {
                existing
            } else {
                val newUser = UserEntity(email = email, passwordHash = "", name = name)
                val id = userDao.insert(newUser).toInt()
                newUser.copy(id = id)
            }
            userPreferences.saveUser(user.id, user.email, user.name)
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed")
        }
    }

    suspend fun updateProfile(user: UserEntity) {
        userDao.update(user)
        userPreferences.saveUser(user.id, user.email, user.name)
    }

    suspend fun getUserById(id: Int): UserEntity? = userDao.findById(id)

    fun observeUser(id: Int): Flow<UserEntity?> = userDao.observeById(id)
}


package com.example.english_app.data.repository

import android.util.Log
import android.util.Patterns
import com.example.english_app.data.local.UserPreferences
import com.example.english_app.data.local.dao.UserDao
import com.example.english_app.data.local.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object VerificationEmailSent : AuthResult()   // đăng ký / resend xong
    object EmailNotVerified : AuthResult()         // login nhưng chưa xác minh email
}

class AuthRepository(
    private val userDao: UserDao,
    val userPreferences: UserPreferences
) {
    private val auth = FirebaseAuth.getInstance()
    val currentUserId: Flow<Int> = userPreferences.userId

    // ── Register ─────────────────────────────────────────────────────────────
    suspend fun register(name: String, email: String, password: String): AuthResult {
        if (name.isBlank()) return AuthResult.Error("Name cannot be empty")
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return AuthResult.Error("Invalid email address")
        if (password.length < 6)
            return AuthResult.Error("Password must be at least 6 characters")

        return try {
            // Tạo tài khoản Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = result.user ?: return AuthResult.Error("Registration failed")

            // Gửi email xác minh
            Log.d("AuthRepo", "Sending verification email to: ${firebaseUser.email}")
            firebaseUser.sendEmailVerification().await()
            Log.d("AuthRepo", "Verification email sent successfully to: ${firebaseUser.email}")

            // Lưu profile vào Room DB (passwordHash = "firebase" làm marker)
            val existing = userDao.findByEmail(email.trim())
            if (existing == null) {
                val newUser = UserEntity(
                    email = email.trim(),
                    passwordHash = "firebase",
                    name = name.trim()
                )
                userDao.insert(newUser)
            }

            // Sign out khỏi Firebase — bắt buộc verify email trước khi login
            auth.signOut()

            AuthResult.VerificationEmailSent
        } catch (e: Exception) {
            Log.e("AuthRepo", "Register error: ${e.message}", e)
            val msg = when {
                e.message?.contains("email address is already in use") == true ->
                    "This email is already registered"
                e.message?.contains("badly formatted") == true ->
                    "Invalid email address"
                else -> e.message ?: "Registration failed"
            }
            AuthResult.Error(msg)
        }
    }

    // ── Login ────────────────────────────────────────────────────────────────
    suspend fun login(email: String, password: String): AuthResult {
        if (email.isBlank()) return AuthResult.Error("Email cannot be empty")
        if (password.isBlank()) return AuthResult.Error("Password cannot be empty")

        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = result.user ?: return AuthResult.Error("Sign in failed")

            // Kiểm tra đã verify email chưa
            if (!firebaseUser.isEmailVerified) {
                auth.signOut()
                return AuthResult.EmailNotVerified
            }

            // Lấy hoặc tạo profile trong Room DB
            val user = userDao.findByEmail(email.trim()) ?: run {
                val newUser = UserEntity(
                    email = email.trim(),
                    passwordHash = "firebase",
                    name = email.trim().substringBefore("@")
                )
                val id = userDao.insert(newUser).toInt()
                newUser.copy(id = id)
            }

            userPreferences.saveUser(user.id, user.email, user.name)
            AuthResult.Success(user)
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("no user record") == true ||
                e.message?.contains("user-not-found") == true -> "Email not found"
                e.message?.contains("password is invalid") == true ||
                e.message?.contains("wrong-password") == true -> "Incorrect password"
                e.message?.contains("badly formatted") == true -> "Invalid email address"
                e.message?.contains("too-many-requests") == true ->
                    "Too many attempts. Please try again later."
                else -> e.message ?: "Sign in failed"
            }
            AuthResult.Error(msg)
        }
    }

    // ── Logout ───────────────────────────────────────────────────────────────
    suspend fun logout() {
        userPreferences.clearUser()
        auth.signOut()
    }

    // ── Google Sign-In ───────────────────────────────────────────────────────
    suspend fun loginWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return AuthResult.Error("Google sign-in failed")
            val email = firebaseUser.email ?: return AuthResult.Error("Could not get email from Google")
            val name = firebaseUser.displayName ?: email.substringBefore("@")
            val existing = userDao.findByEmail(email)
            val user = if (existing != null) {
                existing
            } else {
                val newUser = UserEntity(email = email, passwordHash = "firebase", name = name)
                val id = userDao.insert(newUser).toInt()
                newUser.copy(id = id)
            }
            userPreferences.saveUser(user.id, user.email, user.name)
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed")
        }
    }

    // ── Forgot Password — gửi reset link qua Firebase ────────────────────────
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return AuthResult.Error("Please enter a valid email address")
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.VerificationEmailSent
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("no user record") == true ||
                e.message?.contains("user-not-found") == true ->
                    "Email not registered. Please sign up first."
                else -> e.message ?: "Failed to send reset email"
            }
            AuthResult.Error(msg)
        }
    }

    // ── Resend verification email ─────────────────────────────────────────────
    suspend fun resendVerificationEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            result.user?.sendEmailVerification()?.await()
            auth.signOut()
            AuthResult.VerificationEmailSent
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to resend verification email")
        }
    }

    // ── Profile ───────────────────────────────────────────────────────────────
    suspend fun updateProfile(user: UserEntity) {
        userDao.update(user)
        userPreferences.saveUser(user.id, user.email, user.name)
    }

    suspend fun getUserById(id: Int): UserEntity? = userDao.findById(id)

    fun observeUser(id: Int): Flow<UserEntity?> = userDao.observeById(id)
}


package com.example.english_app.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.english_app.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val webClientId = context.getString(com.example.english_app.R.string.default_web_client_id)

    val googleSignInClient = remember(context, webClientId) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.loginWithGoogle(idToken)
                } else {
                    // idToken null → thường do SHA-1 chưa đúng
                    viewModel.setGoogleError("Google Sign-In thất bại. Kiểm tra SHA-1 fingerprint trong Firebase Console.")
                }
            } catch (e: ApiException) {
                viewModel.setGoogleError("Google Sign-In lỗi (code ${e.statusCode}). Kiểm tra SHA-1 fingerprint.")
            }
        }
        // RESULT_CANCELED = user bấm Back → không làm gì cả
    }

    LaunchedEffect(uiState.success) { if (uiState.success) onLoginSuccess() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))

        // App icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CardBg),
            contentAlignment = Alignment.Center
        ) {
            Text("📖", fontSize = 36.sp)
        }

        Spacer(Modifier.height(20.dp))
        Text("MinLish", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            "Master your vocabulary journey,\none word at a time.",
            fontSize = 14.sp, color = TextSecondary,
            textAlign = TextAlign.Center, lineHeight = 20.sp
        )

        Spacer(Modifier.height(40.dp))

        // Email field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Email", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = TextPrimary, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = email, onValueChange = { email = it; viewModel.clearError() },
                placeholder = { Text("Enter your email", color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Email, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = uiState.error != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedBorderColor = NavyPrimary
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // Password field
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                TextButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {
                    Text("Forgot?", fontSize = 13.sp, color = NavyPrimary)
                }
            }
            OutlinedTextField(
                value = password, onValueChange = { password = it; viewModel.clearError() },
                placeholder = { Text("Enter your password", color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            null, tint = TextSecondary, modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus(); viewModel.login(email, password)
                }),
                isError = uiState.error != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedBorderColor = NavyPrimary
                )
            )
        }

        if (uiState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(uiState.error!!, color = ErrorRed, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(24.dp))

        // Log In button
        Button(
            onClick = { viewModel.login(email, password) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.size(22.dp), color = SurfaceWhite, strokeWidth = 2.dp)
            } else {
                Text("Log In  →", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = SurfaceWhite)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Divider
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE5E7EB))
            Text("  OR CONTINUE WITH  ", fontSize = 11.sp, color = TextSecondary)
            HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE5E7EB))
        }

        Spacer(Modifier.height(16.dp))

        // Google button
        OutlinedButton(
            onClick = {
                viewModel.clearError()
                googleSignInClient.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF4285F4), modifier = Modifier.padding(end = 10.dp))
            Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        Spacer(Modifier.height(32.dp))

        // Sign up link
        TextButton(onClick = onNavigateToRegister) {
            Text(buildAnnotatedString {
                withStyle(SpanStyle(color = TextSecondary, fontSize = 14.sp)) { append("Don't have an account? ") }
                withStyle(SpanStyle(color = NavyPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)) { append("Sign up") }
            })
        }
        Spacer(Modifier.height(24.dp))
    }
}

package com.example.english_app.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
    val forgotState by viewModel.forgotState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
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

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) viewModel.loginWithGoogle(idToken)
                else viewModel.setGoogleError("Google Sign-In thất bại. Kiểm tra SHA-1 fingerprint trong Firebase Console.")
            } catch (e: ApiException) {
                viewModel.setGoogleError("Google Sign-In lỗi (code ${e.statusCode}). Kiểm tra SHA-1 fingerprint.")
            }
        }
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

        Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)).background(CardBg),
            contentAlignment = Alignment.Center) { Text("📖", fontSize = 36.sp) }

        Spacer(Modifier.height(20.dp))
        Text("MinLish", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text("Master your vocabulary journey,\none word at a time.",
            fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 20.sp)

        Spacer(Modifier.height(40.dp))

        // Email field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Email", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = TextPrimary, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = email, onValueChange = { email = it; viewModel.clearError() },
                placeholder = { Text("Enter your email", color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Email, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = uiState.error != null, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = NavyPrimary)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Password field
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                TextButton(onClick = { showForgotDialog = true }, contentPadding = PaddingValues(0.dp)) {
                    Text("Forgot?", fontSize = 13.sp, color = NavyPrimary)
                }
            }
            OutlinedTextField(
                value = password, onValueChange = { password = it; viewModel.clearError() },
                placeholder = { Text("Enter your password", color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.login(email, password) }),
                isError = uiState.error != null, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = NavyPrimary)
            )
        }

        // Error / email-not-verified banner
        if (uiState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(uiState.error!!, color = ErrorRed, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
            if (uiState.emailNotVerified) {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { viewModel.resendVerificationEmail(email, password) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Resend verification email", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                }
            }
            // Show "email sent" confirmation after resend
            if (uiState.verificationSent) {
                Spacer(Modifier.height(4.dp))
                Text("✅ Verification email sent! Check your inbox.", color = Color(0xFF16A34A), fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
            if (uiState.isLoading)
                CircularProgressIndicator(Modifier.size(22.dp), color = SurfaceWhite, strokeWidth = 2.dp)
            else
                Text("Log In  →", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = SurfaceWhite)
        }

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE5E7EB))
            Text("  OR CONTINUE WITH  ", fontSize = 11.sp, color = TextSecondary)
            HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE5E7EB))
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.clearError(); googleSignInClient.signOut().addOnCompleteListener { googleSignInLauncher.launch(googleSignInClient.signInIntent) } },
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

        TextButton(onClick = onNavigateToRegister) {
            Text(buildAnnotatedString {
                withStyle(SpanStyle(color = TextSecondary, fontSize = 14.sp)) { append("Don't have an account? ") }
                withStyle(SpanStyle(color = NavyPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)) { append("Sign up") }
            })
        }
        Spacer(Modifier.height(24.dp))
    }

    // ── Forgot Password Dialog (1 bước — Firebase gửi reset link) ───────────
    if (showForgotDialog) {
        ForgotPasswordDialog(
            forgotState = forgotState,
            onDismiss = { showForgotDialog = false; viewModel.resetForgotState() },
            onSendResetEmail = { viewModel.forgotSendResetEmail(it) }
        )
    }
}

// ─────────────────────── ForgotPasswordDialog (1 bước) ───────────────────────
@Composable
fun ForgotPasswordDialog(
    forgotState: ForgotPasswordState,
    onDismiss: () -> Unit,
    onSendResetEmail: (String) -> Unit
) {
    var emailInput by remember { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = NavyPrimary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (forgotState.emailSent) "Reset Email Sent! 📬" else "Forgot Password",
                    fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (forgotState.emailSent)
                        "Check your inbox and follow the link to reset your password."
                    else
                        "Enter your email and we'll send you a reset link.",
                    fontSize = 13.sp, color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            if (forgotState.emailSent) {
                // Success state
                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text("✅", fontSize = 48.sp)
                }
            } else {
                // Enter email
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email address") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onSendResetEmail(emailInput) }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        isError = forgotState.error != null
                    )
                    if (forgotState.error != null) {
                        Text(forgotState.error, color = ErrorRed, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (forgotState.emailSent) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Back to Login") }
            } else {
                Button(
                    onClick = { onSendResetEmail(emailInput) },
                    enabled = !forgotState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (forgotState.isLoading)
                        CircularProgressIndicator(Modifier.size(18.dp), color = SurfaceWhite, strokeWidth = 2.dp)
                    else
                        Text("Send Reset Link")
                }
            }
        },
        dismissButton = {
            if (!forgotState.emailSent) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
            }
        }
    )
}


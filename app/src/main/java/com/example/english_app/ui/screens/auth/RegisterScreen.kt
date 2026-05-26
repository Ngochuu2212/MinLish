package com.example.english_app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.english_app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.success) { if (uiState.success) onRegisterSuccess() }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = NavyPrimary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth()) {
            IconButton(onClick = onNavigateToLogin, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
            }
        }
        Text("Create Account", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
            modifier = Modifier.fillMaxWidth())
        Text("Start your vocabulary journey", fontSize = 14.sp, color = TextSecondary,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 28.dp))

        fun field(label: String) = Modifier.fillMaxWidth()

        LabeledField("Full Name") {
            OutlinedTextField(value = name, onValueChange = { name = it },
                placeholder = { Text("Your name", color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Person, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = fieldColors)
        }
        Spacer(Modifier.height(12.dp))
        LabeledField("Email") {
            OutlinedTextField(value = email, onValueChange = { email = it },
                placeholder = { Text("your@email.com", color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Email, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = fieldColors)
        }
        Spacer(Modifier.height(12.dp))
        LabeledField("Password") {
            OutlinedTextField(value = password, onValueChange = { password = it },
                placeholder = { Text("Min. 6 characters", color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                }},
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = fieldColors)
        }
        Spacer(Modifier.height(12.dp))
        LabeledField("Confirm Password") {
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it },
                placeholder = { Text("Repeat password", color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = fieldColors)
        }

        val displayError = localError ?: uiState.error
        if (displayError != null) {
            Spacer(Modifier.height(8.dp))
            Text(displayError, color = ErrorRed, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                localError = null
                if (password != confirmPassword) { localError = "Passwords do not match"; return@Button }
                viewModel.register(name, email, password)
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(22.dp), color = SurfaceWhite, strokeWidth = 2.dp)
            else Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(20.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text(buildAnnotatedString {
                withStyle(SpanStyle(color = TextSecondary, fontSize = 14.sp)) { append("Already have an account? ") }
                withStyle(SpanStyle(color = NavyPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)) { append("Sign In") }
            })
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary,
            modifier = Modifier.padding(bottom = 6.dp))
        content()
    }
}


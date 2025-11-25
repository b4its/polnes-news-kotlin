package com.mxlkt.newspolnes.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mxlkt.newspolnes.components.CommonTopBar
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.view.AuthViewModel
import com.mxlkt.newspolnes.view.AuthState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    rootNavController: NavHostController,
    authNavController: NavController,
    // Inject AuthViewModel
    authViewModel: AuthViewModel = viewModel()
) {
    // State untuk form input
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // State untuk error lokal (misal: password tidak cocok)
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    // Observe state dari ViewModel
    val authState = authViewModel.authState.value
    val isLoading = authState is AuthState.Loading

    // State dan scope untuk Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Efek samping untuk menangani navigasi dan error dari API
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SuccessRegister -> {
                // Pendaftaran berhasil, navigasi ke main app dan hapus stack auth
                rootNavController.navigate("user_app") {
                    popUpTo("auth") { inclusive = true }
                }
                // Reset state setelah navigasi
                authViewModel.resetState()
            }
            is AuthState.Error -> {
                // Tampilkan pesan error dari API
                localError = null // Clear local error if API error comes in
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = authState.message,
                        actionLabel = "Tutup",
                        duration = SnackbarDuration.Long
                    )
                    authViewModel.resetState()
                }
            }
            else -> Unit // Abaikan Idle dan Loading
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Sign Up",
                onBack = { authNavController.navigateUp() }
            )
        },
        snackbarHost = {
            // Tampilkan Snackbar untuk error
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Hello!\nSign up to get started!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                lineHeight = 40.sp
            )

            // Input Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Email Address
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Password
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (localError != null) localError = null // Clear error on change
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (localError != null) localError = null // Clear error on change
                },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = if (confirmPasswordVisible) "Sembunyikan password" else "Tampilkan password")
                    }
                }
            )

            // Tampilkan error lokal (misal: password tidak cocok)
            if (localError != null) {
                Text(
                    text = localError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1.0f))

            // Button Sign Up
            Button(
                onClick = {
                    localError = null // Reset error lokal

                    // 1. Validasi lokal (minimal)
                    if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        localError = "Semua kolom harus diisi."
                        return@Button
                    }
                    if (password != confirmPassword) {
                        localError = "Konfirmasi password tidak cocok."
                        return@Button
                    }

                    // 2. Panggil ViewModel untuk mendaftar
                    authViewModel.register(
                        name = fullName,
                        email = email,
                        password = password
                    )

                    authNavController.navigate("login")
                },
                enabled = !isLoading, // Disable tombol saat loading
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Sign Up", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    NewsPolnesTheme {
        SignUpScreen(
            rootNavController = rememberNavController(),
            authNavController = rememberNavController()
        )
    }
}
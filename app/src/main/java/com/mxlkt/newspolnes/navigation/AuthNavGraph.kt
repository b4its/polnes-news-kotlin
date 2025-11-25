package com.mxlkt.newspolnes.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxlkt.newspolnes.auth.LoginScreen
import com.mxlkt.newspolnes.auth.SignUpScreen
import com.mxlkt.newspolnes.auth.WelcomeScreen
import com.mxlkt.newspolnes.view.AuthViewModel
import com.mxlkt.newspolnes.view.AuthState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun AuthNavGraph(
    rootNavController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authNavController = rememberNavController()
    val context = LocalContext.current

    val authState by authViewModel.authState

    // Logika Navigasi Otomatis setelah Login/Register Sukses
    LaunchedEffect(authState) {
        val destination = when (authState) {
            is AuthState.SuccessLogin -> {
                val user = (authState as AuthState.SuccessLogin).user
                when (user.role.name) {
                    "ADMIN" -> "admin_root"
                    "EDITOR" -> "editor_root"
                    "USER" -> "user_root"
                    else -> null
                }
            }
            is AuthState.SuccessRegister -> {
                val user = (authState as AuthState.SuccessRegister).user
                when (user.role.name) {
                    "ADMIN" -> "admin_root"
                    "EDITOR" -> "editor_root"
                    "USER" -> "user_root"
                    else -> null
                }
            }
            is AuthState.Error -> {
                // Tampilkan pesan error
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
                null
            }
            else -> null
        }

        if (destination != null) {
            rootNavController.navigate(destination) {
                popUpTo("auth_graph") { inclusive = true }
            }
            authViewModel.resetState()
        }
    }


    NavHost(
        navController = authNavController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                rootNavController = rootNavController,
                authNavController = authNavController
            )
        }

        composable("login") {
            LoginScreen(
                rootNavController = rootNavController,
                authNavController = authNavController,
                // Gunakan ViewModel untuk memproses otentikasi
                onLoginSubmitted = { emailInput, passwordInput ->
                    authViewModel.login(emailInput, passwordInput)
                }
            )
        }


        composable("signup") {
            // Asumsi: SignUpScreen telah diubah untuk menggunakan AuthViewModel untuk register
            SignUpScreen(
                rootNavController = rootNavController,
                authNavController = authNavController
                // Jika SignUpScreen memiliki onSignUpSubmitted, panggil:
//                 onSignUpSubmitted = { name, email, password ->
//                     authViewModel.register(name, email, password)
//                 }
            )
        }
    }
}
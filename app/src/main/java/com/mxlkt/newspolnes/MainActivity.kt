package com.mxlkt.newspolnes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mxlkt.newspolnes.navigation.AdminNavGraph // � 1. WAJIB IMPORT INI
import com.mxlkt.newspolnes.navigation.AuthNavGraph
import com.mxlkt.newspolnes.navigation.EditorNavGraph
import com.mxlkt.newspolnes.navigation.UserNavGraph
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            NewsPolnesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRootNavigation()
                }
            }
        }
    }
}

@Composable
fun AppRootNavigation() {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = "auth_graph"
    ) {
        // 1. LOGIN
        composable("auth_graph") {
            AuthNavGraph(rootNavController = rootNavController)
        }

        // 2. USER BIASA
        composable("user_root") {
            UserNavGraph(rootNavController = rootNavController)
        }

        // 3. EDITOR
        composable("editor_root") {
            val currentUser = SessionManager.currentUser
            EditorNavGraph(
                rootNavController = rootNavController,
                currentUser = currentUser,
                onLogout = {
                    SessionManager.currentUser = null
                    rootNavController.navigate("auth_graph") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 4. ADMIN (� TAMBAHKAN INI)
        composable("admin_root") {
            val currentUser = SessionManager.currentUser
            AdminNavGraph(
                rootNavController = rootNavController,
                currentUser = currentUser,
                onLogout = {
                    // Hapus sesi user
                    SessionManager.currentUser = null
                    // Kembali ke Login dan hapus stack navigasi sebelumnya
                    rootNavController.navigate("auth_graph") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
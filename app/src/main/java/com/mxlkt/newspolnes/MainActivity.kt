package com.mxlkt.newspolnes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mxlkt.newspolnes.model.UserRole
import com.mxlkt.newspolnes.navigation.AdminNavGraph
import com.mxlkt.newspolnes.navigation.AuthNavGraph
import com.mxlkt.newspolnes.navigation.EditorNavGraph
import com.mxlkt.newspolnes.navigation.UserNavGraph
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.utils.NetworkUtils // --- IMPORT BARU ---
import com.mxlkt.newspolnes.view.AuthViewModel
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.LaunchedEffect

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

// --- KOMPONEN BARU: Menampilkan pesan jika tidak ada internet ---
@Composable
fun NoInternetScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Tidak Ada Koneksi Internet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Mohon periksa pengaturan jaringan Anda dan mulai ulang aplikasi.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
// -----------------------------------------------------------------

@Composable
fun AppRootNavigation(authViewModel: AuthViewModel = viewModel()) {

    val context = LocalContext.current // Mendapatkan konteks untuk cek internet
    val rootNavController = rememberNavController()

    // Ambil status login dan Role pengguna dari DataStore secara reaktif
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)
    val userRoleString by authViewModel.userRole.collectAsState(initial = null)

    var startDestination by remember { mutableStateOf<String?>(null) }
    var isInternetConnected by remember { mutableStateOf(true) } // State untuk koneksi internet

    // Muat State Awal (Initial Route) dan Cek Internet saat composition
    LaunchedEffect(key1 = Unit) {
        // --- Cek Koneksi Internet ---
        if (!NetworkUtils.isInternetAvailable(context)) {
            isInternetConnected = false
            return@LaunchedEffect // Hentikan proses jika tidak ada internet
        }

        // --- Lanjutkan logika pengecekan sesi jika internet OK ---
        val loggedIn = authViewModel.isLoggedIn.first()
        val role = authViewModel.userRole.first()

        startDestination = if (loggedIn == true && role != null) {
            try {
                when (UserRole.valueOf(role)) {
                    UserRole.ADMIN -> "admin_root"
                    UserRole.EDITOR -> "editor_root"
                    UserRole.USER -> "user_root"
                }
            } catch (e: IllegalArgumentException) {
                // Jika peran tidak valid, kembalikan ke login
                "auth_graph"
            }
        } else {
            "auth_graph"
        }
    }

    // Tampilkan layar NoInternet jika koneksi terputus
    if (!isInternetConnected) {
        NoInternetScreen()
        return
    }

    // Tampilkan Loading jika startDestination belum dimuat
    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Fungsi Logout universal yang digunakan oleh semua graf navigasi
    val performLogout: () -> Unit = {
        authViewModel.logout()
        rootNavController.navigate("auth_graph") {
            // Bersihkan semua stack di belakang agar tidak bisa kembali ke halaman yang membutuhkan login
            popUpTo(0) { inclusive = true }
        }
    }


    NavHost(
        navController = rootNavController,
        startDestination = startDestination!!
    ) {
        // --- GRAF NAVIGASI OTENTIKASI ---
        composable("auth_graph") {
            AuthNavGraph(rootNavController = rootNavController)
        }

        // --- GRAF NAVIGASI PENGGUNA BIASA ---
        composable("user_root") {
            UserNavGraph(
                rootNavController = rootNavController,
                onLogout = performLogout // Meneruskan fungsi logout
            )
        }

        // --- GRAF NAVIGASI EDITOR ---
        composable("editor_root") {
            EditorNavGraph(
                rootNavController = rootNavController,
                onLogout = performLogout // Meneruskan fungsi logout
            )
        }

        // --- GRAF NAVIGASI ADMIN ---
        composable("admin_root") {
            AdminNavGraph(
                rootNavController = rootNavController,
                onLogout = performLogout // Meneruskan fungsi logout
            )
        }
    }
}
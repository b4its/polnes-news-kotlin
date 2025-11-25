// File: com/mxlkt/newspolnes/ui/editor/EditorSettingsScreen.kt

package com.mxlkt.newspolnes.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mxlkt.newspolnes.components.AccountInfoCard
import com.mxlkt.newspolnes.components.EditorBottomNav
import com.mxlkt.newspolnes.components.SettingsButton
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
// Hapus import com.mxlkt.newspolnes.utils.SessionManager - sudah benar
// Hapus import yang tidak digunakan

@Composable
fun EditorSettingsScreen(
    navController: NavHostController,
    // � PERBAIKAN: Parameter data pengguna yang dioper dari NavHost
    userName: String,
    userRole: String,
    onLogout: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TitleOnlyTopAppBar(title = "Settings") }
        // BottomBar biasanya diletakkan di Root/Main Screen, bukan di sini.
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding) // Penting untuk menghindari tumpang tindih dengan TopBar/BottomBar
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            // --- Bagian Info Akun ---
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            AccountInfoCard(
                fullName = userName,
                role = userRole,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Bagian Tombol-Tombol Pengaturan ---
            Text(
                text = "More Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            SettingsButton(
                text = "Privacy and Policy",
                icon = Icons.Outlined.PrivacyTip,
                onClick = onPrivacyClick,
                // � PERBAIKAN: Tambahkan modifier padding
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            SettingsButton(
                text = "About",
                icon = Icons.Outlined.Info,
                onClick = onAboutClick,
                // � PERBAIKAN: Tambahkan modifier padding
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Tombol Logout
            SettingsButton(
                text = "Logout",
                icon = Icons.Outlined.Logout,
                onClick = onLogout,
                // � PERBAIKAN: Tambahkan modifier padding
                modifier = Modifier.padding(horizontal = 16.dp),
                contentColor = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- PREVIEW ---
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun EditorSettingsScreenPreview() {
    NewsPolnesTheme {
        Scaffold(
            topBar = { TitleOnlyTopAppBar(title = "Settings") },
            bottomBar = { EditorBottomNav(currentRoute = "editor_settings", onNavigate = {}) }
        ) { innerPadding ->
            // Modifier.padding(innerPadding) diterapkan ke EditorSettingsScreen
            EditorSettingsScreen(
                navController = rememberNavController(),
                userName = "Prof. Budi Santoso",
                userRole = "Editor",
                onLogout = {},
                onPrivacyClick = {},
                onAboutClick = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
package com.mxlkt.newspolnes.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxlkt.newspolnes.components.AccountInfoCard
import com.mxlkt.newspolnes.components.SettingsButton
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.components.UserBottomNav
// import com.mxlkt.newspolnes.model.DummyData // 🔴 Hapus ini (tidak dipakai lagi)
import com.mxlkt.newspolnes.utils.SessionManager // 🟢 Import SessionManager

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    // 🟢 REVISI: Ambil data user yang sedang LOGIN dari SessionManager
    val currentUser = SessionManager.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        // --- Bagian Info Akun ---
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Menampilkan data user asli
        AccountInfoCard(
            fullName = currentUser?.name ?: "Guest User", // Tampil Guest jika null
            role = currentUser?.role?.name?.let {
                it.lowercase().replaceFirstChar { char -> char.uppercase() }
            } ?: "Guest"
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
            onClick = onPrivacyClick
        )
        SettingsButton(
            text = "About",
            icon = Icons.Outlined.Info,
            onClick = onAboutClick
        )

        // Tombol Logout
        SettingsButton(
            text = "Logout",
            icon = Icons.Outlined.Logout,
            onClick = onLogout
        )
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        onLogout = {},
        onPrivacyClick = {},
        onAboutClick = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun FullSettingsScreenPreview() {
    Scaffold(
        topBar = { TitleOnlyTopAppBar(title = "Settings") },
        bottomBar = { UserBottomNav(currentRoute = "Settings", onItemClick = {}) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            SettingsScreen(
                onLogout = {},
                onPrivacyClick = {},
                onAboutClick = {}
            )
        }
    }
}
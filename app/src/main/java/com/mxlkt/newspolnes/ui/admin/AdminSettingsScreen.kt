package com.mxlkt.newspolnes.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxlkt.newspolnes.components.AccountInfoCard
import com.mxlkt.newspolnes.components.AdminBottomNav
import com.mxlkt.newspolnes.components.SettingsButton // Import komponen yang diperbaiki
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.UserRole
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.utils.SessionManager

@Composable
fun AdminSettingsScreen(
    onLogout: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // � PERBAIKAN: Ambil ID user dari SessionManager (Flow)
    val loggedInUserId by sessionManager.userId.collectAsState(initial = null)

    // � Cari objek User Admin yang sedang login
    val currentUser = remember(loggedInUserId) {
        loggedInUserId?.let { id -> StoreData.userList.find { it.id == id && it.role == UserRole.ADMIN } }
    }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            // --- Info Akun ---
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            AccountInfoCard(
                fullName = currentUser?.name ?: "Admin",
                role = currentUser?.role?.name ?: "Administrator",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Pengaturan Umum ---
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
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            SettingsButton(
                text = "About",
                icon = Icons.Outlined.Info,
                onClick = onAboutClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Tombol Logout
            SettingsButton(
                text = "Logout",
                icon = Icons.Outlined.Logout,
                onClick = onLogout,
                modifier = Modifier.padding(horizontal = 16.dp),
                // � Menggunakan contentColor untuk memicu Error State pada komponen SettingsButton
                contentColor = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }


@Preview(showBackground = true)
@Composable
private fun AdminSettingsFullPreview() {
    NewsPolnesTheme {
        AdminSettingsScreen(
            onLogout = {},
            onPrivacyClick = {},
            onAboutClick = {}
        )
    }
}
package com.mxlkt.newspolnes.components

import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.mxlkt.newspolnes.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleFormTopBar(
    title: String,
    isEditMode: Boolean,
    onBackClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    // 1. Logic: Paksa ikon status bar (jam, baterai) jadi Putih
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // False = Ikon Putih (Cocok untuk background hijau gelap)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // 2. Konfigurasi Warna
    val topBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
    )

    // 3. UI Top Bar
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        // 🟢 KUNCI UTAMA: Paksa insets jadi 0 agar menempel ke atas (Status Bar)
        windowInsets = WindowInsets(0.dp),
        colors = topBarColors,

        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }
        },
        actions = {
            if (isEditMode) {
                IconButton(onClick = onDeleteRequest) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Article",
                        tint = White
                    )
                }
            }
        }
    )
}
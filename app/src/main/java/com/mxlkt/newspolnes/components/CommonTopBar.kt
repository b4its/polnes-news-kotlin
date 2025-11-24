package com.mxlkt.newspolnes.components

import android.app.Activity
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},

    // 🟢 Default Insets (Agar kompatibel dengan scaffold biasa)
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,

    // 🟢 Default Warna: TETAP HIJAU (Primary)
    // Jadi halaman lain yang tidak setting warna, otomatis dapet warna ini.
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary, // Hijau
        titleContentColor = MaterialTheme.colorScheme.onPrimary, // Putih
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
    )
) {
    // Logic agar ikon status bar (jam/baterai) jadi putih (karena background hijau)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Paksa ikon status bar jadi Putih biar kelihatan di atas warna Hijau
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali"
                    )
                }
            }
        },
        actions = actions,

        // Gunakan parameter yang dikirim (atau default hijaunya)
        colors = colors,
        windowInsets = windowInsets
    )
}
package com.mxlkt.newspolnes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun NewsPolnesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Android 12+) bisa dimatikan jika Anda ingin
    // warna hijau Anda 100% konsisten di semua HP.
    dynamicColor: Boolean = false, // Saya ganti ke 'false' agar selalu hijau
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // Ini sekarang akan merujuk ke 'DarkColorScheme' HIJAU dari Color.kt
        darkTheme -> DarkColorScheme

        // Ini sekarang akan merujuk ke 'LightColorScheme' HIJAU dari Color.kt
        else -> LightColorScheme
    }

    // Ini kode tambahan untuk mengubah warna status bar (bar di atas)
    // agar warnanya sama dengan warna primary (hijau)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Pastikan file Typography.kt Anda ada
        content = content
    )
}
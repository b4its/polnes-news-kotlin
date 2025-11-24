package com.mxlkt.newspolnes.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun EditorBottomNav(
    modifier: Modifier = Modifier,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // 1. URUTAN BARU: Dashboard -> Articles -> Settings
    val items = listOf(
        "editor_dashboard",
        "editor_articles",
        "editor_settings"
    )

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 3.dp
    ) {
        items.forEach { route ->
            // Cek apakah route saat ini sama dengan item
            val isSelected = currentRoute == route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(route) },
                icon = {
                    Icon(
                        imageVector = getIconForEditorRoute(route),
                        contentDescription = getLabelForEditorRoute(route)
                    )
                },
                label = {
                    Text(
                        text = getLabelForEditorRoute(route),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                },
                // Styling warna persis sama dengan UserBottomNav
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

// Helper untuk mendapatkan Label
@Composable
private fun getLabelForEditorRoute(route: String): String {
    return when (route) {
        "editor_dashboard" -> "Dashboard"
        "editor_articles" -> "Your Articles"
        "editor_settings" -> "Settings"
        else -> "Menu"
    }
}

// Helper untuk mendapatkan Ikon
@Composable
private fun getIconForEditorRoute(route: String): ImageVector {
    return when (route) {
        "editor_dashboard" -> Icons.Default.BarChart
        "editor_articles" -> Icons.Default.Article
        "editor_settings" -> Icons.Default.Settings
        else -> Icons.Default.BarChart
    }
}
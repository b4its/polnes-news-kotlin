package com.mxlkt.newspolnes.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AdminBottomNav(
    modifier: Modifier = Modifier,
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    // Daftar Menu untuk Admin
    val items = listOf("Dashboard", "News", "Categories", "Users", "Settings")

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 3.dp
    ) {
        items.forEach { label ->
            // Logika seleksi: Cocokkan route
            val isSelected = currentRoute == label

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(label) },
                icon = {
                    Icon(
                        imageVector = getAdminIconForLabel(label),
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall, // Pakai Small biar muat 5 item
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                },
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

@Composable
private fun getAdminIconForLabel(label: String): ImageVector {
    return when (label) {
        "Dashboard" -> Icons.Default.Dashboard
        "News" -> Icons.Default.Article // Ikon koran/artikel
        "Categories" -> Icons.Default.Category
        "Users" -> Icons.Default.Group // Ikon user banyak
        "Settings" -> Icons.Default.Settings
        else -> Icons.Default.Circle
    }
}
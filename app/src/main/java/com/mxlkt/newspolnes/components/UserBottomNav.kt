package com.mxlkt.newspolnes.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun UserBottomNav(
    modifier: Modifier = Modifier,
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    val items = listOf("Home", "Categories", "Notifications", "Settings")

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 3.dp // 🔹 Tambahkan sedikit elevasi biar ada depth
    ) {
        items.forEach { label ->
            val isSelected = (currentRoute == label)

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(label) },
                icon = {
                    Icon(
                        imageVector = getIconForLabel(label),
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
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
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) // 🔹 Biar lebih lembut
                )
            )
        }
    }
}

@Composable
private fun getIconForLabel(label: String): ImageVector {
    return when (label) {
        "Home" -> Icons.Filled.Home
        "Categories" -> Icons.Filled.Category
        "Notifications" -> Icons.Filled.Notifications
        "Settings" -> Icons.Filled.Settings
        else -> Icons.Filled.Home
    }
}

package com.mxlkt.newspolnes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxlkt.newspolnes.model.User
import com.mxlkt.newspolnes.model.UserRole
import com.mxlkt.newspolnes.ui.theme.* // Import warna tema

@Composable
fun AdminUserCard(
    user: User,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // 1. Tentukan Warna Berdasarkan Role (Agar Avatar & Chip Senada)
    val (mainColorBg, mainColorText) = when (user.role) {
        UserRole.EDITOR -> Pair(StatusPendingBg, StatusPendingText)     // Biru
        UserRole.USER -> Pair(StatusPublishedBg, StatusPublishedText)   // Hijau
        UserRole.ADMIN -> Pair(StatusDraftBg, StatusDraftText)          // Kuning/Orange
    }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Avatar (Mengikuti Warna Role) ---
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(mainColorBg), // Background sesuai role
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = mainColorText // Tint ikon sesuai role
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- Info User ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))

                // --- Role Chip (Gaya StatusChip) ---
                UserRoleChip(role = user.role)
            }

            // --- Tombol Aksi ---
            // Edit
            FilledIconButton(
                onClick = onEditClick,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ActionEditBg,
                    contentColor = ActionEditIcon
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete
            FilledIconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ActionDeleteBg,
                    contentColor = ActionDeleteIcon
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun UserRoleChip(role: UserRole) {
    // Logika Warna: Mengikuti pola StatusChip
    val (bgColor, textColor) = when (role) {
        UserRole.EDITOR -> Pair(StatusPendingBg, StatusPendingText)     // Biru
        UserRole.USER -> Pair(StatusPublishedBg, StatusPublishedText)   // Hijau
        UserRole.ADMIN -> Pair(StatusDraftBg, StatusDraftText)          // Kuning
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50) // Bentuk kapsul penuh
    ) {
        Text(
            text = role.name, // Menampilkan "EDITOR", "USER", dll
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminUserCardPreview() {
    NewsPolnesTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Preview Editor
            AdminUserCard(
                user = User(1, "Editor Name", "editor@polnes.id", "pass", UserRole.EDITOR),
                onEditClick = {}, onDeleteClick = {}
            )
            // Preview User Biasa
            AdminUserCard(
                user = User(2, "User Name", "user@gmail.com", "pass", UserRole.USER),
                onEditClick = {}, onDeleteClick = {}
            )
        }
    }
}
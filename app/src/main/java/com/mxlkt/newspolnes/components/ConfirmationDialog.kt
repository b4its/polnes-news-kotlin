package com.mxlkt.newspolnes.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
// Import warna dari theme agar bisa dipakai langsung jika perlu
import com.mxlkt.newspolnes.ui.theme.ActionDeleteIcon

@Composable
fun ConfirmationDialog(
    title: String = "Konfirmasi",
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String = "Ya",
    dismissButtonText: String = "Batal",
    // Default tombol 'Ya' mengikuti warna Primary (PolnesGreen)
    confirmButtonColor: Color = MaterialTheme.colorScheme.primary
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface, // Putih / DarkGray
        titleContentColor = MaterialTheme.colorScheme.onSurface, // Hitam / Putih
        textContentColor = MaterialTheme.colorScheme.onSurface,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmButtonText,
                    color = confirmButtonColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissButtonText,
                    // Tombol Batal kita beri warna netral (onSurface) atau Primary,
                    // di sini saya pakai onSurface agar tidak mendominasi.
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}
package com.mxlkt.newspolnes.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Dialog konfirmasi yang dapat digunakan kembali untuk menghapus item.
 *
 * @param showDialog Tentukan true untuk menampilkan dialog.
 * @param onDismiss Dipanggil saat dialog ditutup (klik di luar atau tombol "Cancel").
 * @param onConfirm Dipanggil saat tombol "Delete" diklik.
 */
@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss, // Aksi saat klik di luar dialog

            // Judul dialog
            title = {
                Text(text = "Confirm Deletion")
            },

            // Pesan utama
            text = {
                Text("Are you sure you want to delete this?")
            },

            // Tombol konfirmasi (Delete)
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm() // Jalankan aksi hapus
                        onDismiss() // Tutup dialog
                    }
                ) {
                    Text("Delete") // Tombol positif
                }
            },

            // Tombol batal (Cancel)
            dismissButton = {
                TextButton(
                    onClick = onDismiss // Cukup tutup dialog
                ) {
                    Text("Cancel") // Tombol negatif
                }
            }
        )
    }
}
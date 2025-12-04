package com.mxlkt.newspolnes.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Dialog konfirmasi yang dapat digunakan kembali untuk menghapus item.
 *
 * @param showDialog Tentukan true untuk menampilkan dialog.
 * @param title Judul yang ditampilkan di bagian atas dialog (default: "Konfirmasi Hapus").
 * @param message Pesan utama yang meminta konfirmasi dari pengguna.
 * @param onDismiss Dipanggil saat dialog ditutup (klik di luar atau tombol "Batal").
 * @param onConfirm Dipanggil saat tombol "Hapus" diklik.
 */
@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean,
    title: String = "Konfirmasi Hapus",
    message: String = "Apakah Anda yakin ingin menghapus item ini?",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss, // Aksi saat klik di luar dialog atau tombol Dismiss

            // Judul dialog (menggunakan parameter kustom)
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
            },

            // Pesan utama (menggunakan parameter kustom)
            text = {
                Text(message)
            },

            // Tombol konfirmasi (Hapus)
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm() // Jalankan aksi hapus yang ditentukan pemanggil
                        // Catatan: Asumsi onConfirm() akan menangani penutupan state dialog di ViewModel/Screen
                        // Jika tidak, Anda bisa memanggil onDismiss() di sini juga:
                        // onDismiss()
                    }
                ) {
                    // Menggunakan warna yang menunjukkan bahaya (Danger)
                    Text(
                        "Hapus",
                        color = MaterialTheme.colorScheme.error // Biasanya merah/error untuk aksi delete
                    )
                }
            },

            // Tombol batal (Cancel)
            dismissButton = {
                TextButton(
                    onClick = onDismiss // Cukup tutup dialog
                ) {
                    Text("Batal") // Tombol negatif
                }
            }
        )
    }
}
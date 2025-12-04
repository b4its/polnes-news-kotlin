package com.mxlkt.newspolnes.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mxlkt.newspolnes.model.Comment
import com.mxlkt.newspolnes.model.CommentMeta
import com.mxlkt.newspolnes.repository.CommentRepository
import kotlinx.coroutines.launch

class CommentViewModel(application: Application) : AndroidViewModel(application) {

    // Instance Repository
    private val repository = CommentRepository()

    // --- LiveData untuk State UI ---

    // Daftar Komentar
    private val _commentList = MutableLiveData<List<Comment>>(emptyList())
    val commentList: LiveData<List<Comment>> = _commentList

    // Meta Data (Total Rating & Rata-rata Rating)
    // Berguna untuk menampilkan "Rating: 4.5 (10 ulasan)" di UI
    private val _commentMeta = MutableLiveData<CommentMeta?>()
    val commentMeta: LiveData<CommentMeta?> = _commentMeta

    // State Loading
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Pesan Error
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Pesan Sukses (misal: "Komentar berhasil dikirim")
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // --- Fungsi API ---

    /**
     * Mengambil daftar komentar berdasarkan ID Berita.
     * Mengisi commentList dan commentMeta.
     */
    fun fetchComments(newsId: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            // Memanggil repository
            val result = repository.getComments(newsId)

            // Menangani hasil Result<T>
            result.onSuccess { response ->
                _commentList.value = response.data // List komentar
                _commentMeta.value = response.meta // Meta data (rating rata-rata)
            }.onFailure { exception ->
                _errorMessage.value = "Gagal memuat komentar: ${exception.message}"
            }

            _isLoading.value = false
        }
    }

    /**
     * Mengirim komentar dan rating baru.
     * Jika sukses, otomatis memuat ulang daftar komentar agar tampilan update.
     */
    fun storeComment(newsId: Int, userId: Int, rating: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            val result = repository.storeComment(newsId, userId, rating)

            result.onSuccess { response ->
                _successMessage.value = response.message

                // Refresh list komentar setelah berhasil posting
                // Agar user langsung melihat komentarnya muncul
                fetchComments(newsId)
            }.onFailure { exception ->
                _errorMessage.value = "Gagal mengirim komentar: ${exception.message}"
                _isLoading.value = false // Matikan loading jika gagal (jika sukses, loading dimatikan di getComments)
            }
        }
    }

    fun updateComment(newsId: Int, userId: Int, rating: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            val result = repository.updateComment(newsId, userId, rating)

            result.onSuccess { response ->
                _successMessage.value = "Rating berhasil diperbarui"

                // PENTING: Refresh list setelah update
                fetchComments(newsId)
            }.onFailure { exception ->
                // Jika error 404 (user belum pernah komen), pesan error akan tertangkap di sini
                _errorMessage.value = "Gagal update: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Mereset pesan status agar tidak muncul berulang kali (misal saat rotasi layar)
     */
    fun clearStatusMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
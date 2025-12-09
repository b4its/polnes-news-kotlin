package com.mxlkt.newspolnes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.model.NewsModel // Pastikan model ini sudah ada
import com.mxlkt.newspolnes.repository.CategoryRepository
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel untuk mengelola data kategori dan berita di dalamnya.
 * Menangani logic bisnis dan komunikasi antara Repository dan UI.
 */
class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    // Instance Repository
    private val repository = CategoryRepository()

    // ==========================================
    // LIVE DATA (STATE UI)
    // ==========================================

    // 1. Daftar Berita berdasarkan Kategori (FIX: Ditambahkan public val)
    private val _newsInCategoryList = MutableLiveData<List<NewsModel>>(emptyList())
    val newsInCategoryList: LiveData<List<NewsModel>> = _newsInCategoryList

    // 2. Daftar Kategori (Untuk Menu Utama / Admin)
    private val _categoryList = MutableLiveData<List<Category>>(emptyList())
    val categoryList: LiveData<List<Category>> = _categoryList

    // 3. State Loading (Spinner)
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // 4. Pesan Error (Untuk ditampilkan di Snackbar/Text Merah)
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // 5. Pesan Sukses (Feedback setelah Create/Update/Delete)
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // 6. Single Category (Hasil return dari Create/Update)
    private val _singleCategory = MutableLiveData<Category?>()
    val singleCategory: LiveData<Category?> = _singleCategory


    // ==========================================
    // FUNGSI API (ACTION)
    // ==========================================

    /**
     * Mengambil semua kategori (READ ALL).
     */
    fun fetchAllCategories() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val categories = repository.getAllCategories()
                _categoryList.value = categories
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat kategori: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mengambil berita spesifik berdasarkan ID Kategori.
     * @param categoryId ID Kategori yang dipilih user.
     * @param page Halaman paginasi (default 1).
     */
    fun fetchNewsByCategory(categoryId: Int, page: Int = 1) {
        _isLoading.value = true
        _errorMessage.value = null

        // Reset list jika memuat halaman pertama agar UI bersih dari data lama
        if (page == 1) {
            _newsInCategoryList.value = emptyList()
        }

        viewModelScope.launch {
            try {
                // Panggil repository
                val paginationResult = repository.getNewsByCategory(categoryId, page)

                // Update LiveData dengan list berita yang didapat
                _newsInCategoryList.value = paginationResult.newsList

                // Cek jika kosong
                if (paginationResult.newsList.isEmpty()) {
                    _errorMessage.value = "Tidak ada berita di kategori ini."
                }

            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat berita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Membuat kategori baru (CREATE).
     */
    fun createCategory(name: String, imageFile: File) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                val newCategory = repository.createCategory(name, imageFile)
                _successMessage.value = "Kategori ${newCategory.name} berhasil dibuat!"
                _singleCategory.value = newCategory

                // Refresh list agar data baru muncul
                fetchAllCategories()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal membuat kategori: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Memperbarui kategori (UPDATE).
     */
    fun updateCategory(categoryId: Int, name: String, imageFile: File?) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                val updatedCategory = repository.updateCategory(categoryId, name, imageFile)
                _successMessage.value = "Kategori ${updatedCategory.name} berhasil diperbarui!"
                _singleCategory.value = updatedCategory

                // Refresh list agar perubahan terlihat
                fetchAllCategories()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memperbarui kategori: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Menghapus kategori (DELETE).
     */
    fun deleteCategory(categoryId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                val message = repository.deleteCategory(categoryId)
                _successMessage.value = message

                // Refresh list agar item hilang dari layar
                fetchAllCategories()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus kategori: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Membersihkan pesan status (misal setelah Toast muncul).
     */
    fun clearStatusMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
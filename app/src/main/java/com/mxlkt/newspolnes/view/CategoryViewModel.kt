package com.mxlkt.newspolnes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.model.CategoryRequest
import com.mxlkt.newspolnes.repository.CategoryRepository
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel untuk mengelola data kategori, menyesuaikan pola dari NewsViewModel.
 */
class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    // Instance Repository (Diinisialisasi secara sederhana)
    private val repository = CategoryRepository()

    // --- LiveData untuk State UI ---

    // Daftar Kategori (berisi list data kategori)
    private val _categoryList = MutableLiveData<List<Category>>(emptyList())
    val categoryList: LiveData<List<Category>> = _categoryList

    // State Loading
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Pesan Error
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Pesan Sukses untuk operasi CUD
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // Data kategori tunggal hasil operasi CREATE/UPDATE
    private val _singleCategory = MutableLiveData<Category?>()
    val singleCategory: LiveData<Category?> = _singleCategory


    // --- Fungsi API ---

    /**
     * Memuat daftar semua kategori.
     */
    fun fetchAllCategories() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                // Repository mengembalikan List<Category> atau melempar Exception
                val categories: List<Category> = repository.getAllCategories()
                _categoryList.value = categories
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat kategori: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Membuat kategori baru.
     */
    fun createCategory(name: String, imageFile: File) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                // Panggil fungsi repository yang baru (yang support Multipart)
                // Kita pass 'name' dan 'imageFile' langsung
                val newCategory: Category = repository.createCategory(name, imageFile)

                _successMessage.value = "Kategori ${newCategory.name} berhasil dibuat!"
                _singleCategory.value = newCategory

                // Refresh list agar data baru muncul
                fetchAllCategories()

            } catch (e: Exception) {
                _errorMessage.value = "Gagal membuat kategori: ${e.message}"
                // Log error untuk debugging jika perlu
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Memperbarui kategori yang sudah ada.
     */
    fun updateCategory(categoryId: Int, name: String, imageFile: File?) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                // Panggil repository dengan parameter baru (id, name, file)
                val updatedCategory: Category = repository.updateCategory(categoryId, name, imageFile)

                _successMessage.value = "Kategori ${updatedCategory.name} berhasil diperbarui!"
                _singleCategory.value = updatedCategory

                // Refresh list agar perubahan langsung terlihat di UI
                fetchAllCategories()

            } catch (e: Exception) {
                _errorMessage.value = "Gagal memperbarui kategori: ${e.message}"
                // Log untuk debugging
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Menghapus kategori berdasarkan ID.
     */
    fun deleteCategory(categoryId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                // 1. Panggil Repository
                val message = repository.deleteCategory(categoryId)

                // 2. Set pesan sukses
                _successMessage.value = message

                // 3. PENTING: Refresh data agar item yang dihapus hilang dari layar
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
     * Fungsi untuk mereset pesan status
     */
    fun clearStatusMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
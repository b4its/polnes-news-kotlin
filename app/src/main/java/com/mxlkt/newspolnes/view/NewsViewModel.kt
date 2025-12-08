package com.mxlkt.newspolnes.view

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mxlkt.newspolnes.data.NewsRepository
import com.mxlkt.newspolnes.model.BasicResponse
import com.mxlkt.newspolnes.model.NewsCreateRequest
import com.mxlkt.newspolnes.model.NewsModel
import kotlinx.coroutines.launch
import java.io.File

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    // Instance Repository (Dependency Injection sederhana)
    private val repository = NewsRepository()

    // --- LiveData untuk State UI ---
    // Daftar Berita (untuk layar Index/List)
    private val _newsList = MutableLiveData<List<NewsModel>>(emptyList())
    val newsList: LiveData<List<NewsModel>> = _newsList

    // Detail Berita Tunggal (untuk layar Detail)
    private val _newsDetail = MutableLiveData<NewsModel?>()
    val newsDetail: LiveData<NewsModel?> = _newsDetail

    // � LIVE DATA BARU UNTUK BERITA TUNGGAL PERTAMA
    private val _recentNewsFirst = MutableLiveData<NewsModel?>()
    val recentNewsFirst: LiveData<NewsModel?> = _recentNewsFirst

    private val _mostViewedFirst = MutableLiveData<NewsModel?>()
    val mostViewedFirst: LiveData<NewsModel?> = _mostViewedFirst

    private val _mostRatedFirst = MutableLiveData<NewsModel?>()
    val mostRatedFirst: LiveData<NewsModel?> = _mostRatedFirst
    // -----------------------------------------------------

    // State Loading
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Pesan Error
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Pesan Sukses untuk operasi CUD (Create/Update/Delete)
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage


    // State khusus untuk hasil penambahan views: menyimpan jumlah views terbaru (Int)
    private val _viewsUpdateResult = MutableLiveData<Int>()
    val viewsUpdateResult: LiveData<Int> = _viewsUpdateResult

    // --- Fungsi API ---



    /**
     * Memuat daftar berita dari API (dengan pagination)
     */




    fun fetchNewsList(page: Int = 1) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getNewsList(page)
                // Jika halaman 1, ganti list. Jika halaman > 1, tambahkan ke list yang sudah ada.
                if (page == 1) {
                    _newsList.value = response.data.data
                } else {
                    val currentList = _newsList.value.orEmpty().toMutableList()
                    currentList.addAll(response.data.data)
                    _newsList.value = currentList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat berita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }




    fun fetchPublishedNewsList(page: Int = 1) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getNewsPublished(page)
                // Jika halaman 1, ganti list. Jika halaman > 1, tambahkan ke list yang sudah ada.
                if (page == 1) {
                    _newsList.value = response.data.data
                } else {
                    val currentList = _newsList.value.orEmpty().toMutableList()
                    currentList.addAll(response.data.data)
                    _newsList.value = currentList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat berita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }




    /**
     * Memuat daftar berita paling banyak dilihat dari API (dengan pagination)
     */


    fun fetchNewsMostViewedLongList(page: Int = 1) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getMostViewedLongList(page)
                // Jika halaman 1, ganti list. Jika halaman > 1, tambahkan ke list yang sudah ada.
                if (page == 1) {
                    _newsList.value = response.data.data
                } else {
                    val currentList = _newsList.value.orEmpty().toMutableList()
                    currentList.addAll(response.data.data)
                    _newsList.value = currentList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat berita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchNewsMostViewedShortList(page: Int = 1) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getMostViewedShortList(page)
                // Jika halaman 1, ganti list. Jika halaman > 1, tambahkan ke list yang sudah ada.
                if (page == 1) {
                    _newsList.value = response.data.data
                } else {
                    val currentList = _newsList.value.orEmpty().toMutableList()
                    currentList.addAll(response.data.data)
                    _newsList.value = currentList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat berita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Memuat daftar berita paling banyak dirating dari API (dengan pagination)
     */


    fun fetchNewsMostRatedLongList(page: Int = 1) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getMostRatedLongList(page)
                // Jika halaman 1, ganti list. Jika halaman > 1, tambahkan ke list yang sudah ada.
                if (page == 1) {
                    _newsList.value = response.data.data
                } else {
                    val currentList = _newsList.value.orEmpty().toMutableList()
                    currentList.addAll(response.data.data)
                    _newsList.value = currentList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat berita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun fetchNewsMostRatedShortList(page: Int = 1) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getMostRatedShortList(page)
                // Jika halaman 1, ganti list. Jika halaman > 1, tambahkan ke list yang sudah ada.
                if (page == 1) {
                    _newsList.value = response.data.data
                } else {
                    val currentList = _newsList.value.orEmpty().toMutableList()
                    currentList.addAll(response.data.data)
                    _newsList.value = currentList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat berita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // � FUNGSI BARU UNTUK MENGAMBIL BERITA TUNGGAL PERTAMA �

    /**
     * Mengambil Berita Terbaru (Recent) pertama (tunggal).
     */
    fun fetchRecentViewFirst() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getRecentViewFirst()
                _recentNewsFirst.value = response.data
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat berita terbaru: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mengambil Berita Paling Banyak Dilihat (Most Viewed) pertama (tunggal).
     */
    fun fetchMostViewedFirst() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getMostViewedFirst()
                _mostViewedFirst.value = response.data
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat berita paling dilihat: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mengambil Berita Paling Banyak Dirating (Most Rated) pertama (tunggal).
     */
    fun fetchMostRatedFirst() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getMostRatedFirst()
                _mostRatedFirst.value = response.data
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat berita paling dirating: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    // -----------------------------------------------------

    /**
     * Memuat detail berita berdasarkan ID
     */
    fun fetchNewsDetail(newsId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        _newsDetail.value = null // Bersihkan detail sebelumnya
        viewModelScope.launch {
            try {
                val response = repository.getNewsDetail(newsId)
                _newsDetail.value = response.data
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat detail berita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Membuat berita baru (tanpa gambar)
     */
// Tambahkan parameter imageFile: File?
    fun createNews(request: NewsCreateRequest, imageFile: File?, thumbnailFile: File?) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                // PERBAIKAN:
                // Karena di langkah sebelumnya kita mendefinisikan Repository sebagai:
                // createNews(request: NewsCreateRequest, imageFile: File?)
                // Maka kita cukup mengirim object request & file saja.

                val response = repository.createNews(request, imageFile, thumbnailFile)

                _successMessage.value = response.message ?: "Berita berhasil dibuat!"

            } catch (e: Exception) {
                _errorMessage.value = "Gagal membuat berita: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Memperbarui berita (dengan/tanpa gambar)
     * Menggunakan overload dengan parameter eksplisit agar mudah dipanggil dari UI
     */
    fun addViews(idNews: Int) {
        // Operasi ini biasanya berjalan di latar belakang tanpa memblokir UI utama.
        // Oleh karena itu, kita tidak menggunakan _isLoading utama.

        viewModelScope.launch {
            try {
                // Panggil fungsi addViews dari Repository
                val response = repository.addViews(idNews)

                // Ambil nilai views terbaru dari objek respons
                // ASUMSI: Kolom 'views' ada di dalam data model berita (SingleNewsResponse.NewsData)
                val newViews = response.data?.views

                if (newViews != null) {
                    // Update LiveData untuk views terbaru
                    _viewsUpdateResult.postValue(newViews)
                    Log.d("NewsViewModel", "Views for ID $idNews updated to $newViews")

                    // Opsional: Jika NewsDetail sedang ditampilkan, perbarui detailnya
                    if (_newsDetail.value?.id == idNews) {
                        _newsDetail.postValue(response.data)
                    }
                } else {
                    Log.e("NewsViewModel", "Views data is missing in the response for ID $idNews.")
                }

            } catch (e: Exception) {
                // Log error jika penambahan views gagal (misal: Not Found 404, atau Network Error)
                Log.e("NewsViewModel", "Failed to add views for ID $idNews: ${e.message}", e)
                // Catatan: Error di sini biasanya tidak ditampilkan ke pengguna karena
                // views adalah fungsi sekunder. Tapi jika perlu, bisa menggunakan _errorMessage.
            }
        }
    }


    fun updateNews(
        newsId: Int,
        title: String,
        content: String,
        authorId: Int,
        categoryId: Int?,
        linkYoutube: String?,
        status: String?,
        imageFile: File?,
        thumbnailFile: File? // <--- TAMBAHKAN PARAMETER INI
    ) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null
        viewModelScope.launch {
            try {
                // Teruskan thumbnailFile ke repository
                val response = repository.updateNews(
                    newsId, title, content, authorId, categoryId, linkYoutube, status, imageFile, thumbnailFile
                )
                _successMessage.value = response.message ?: "Berita berhasil diperbarui!"
                _newsDetail.value = response.data
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memperbarui berita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }



    /**
     * Menghapus berita
     */
    fun deleteNews(newsId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null
        viewModelScope.launch {
            try {
                val response: BasicResponse = repository.deleteNews(newsId)
                _successMessage.value = response.message ?: "Berita berhasil dihapus!"
                // Opsional: Hapus dari daftar di _newsList jika ada
                _newsList.value = _newsList.value?.filter { it.id != newsId }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus berita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi untuk mereset pesan status
    fun clearStatusMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
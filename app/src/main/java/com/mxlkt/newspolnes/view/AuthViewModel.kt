package com.mxlkt.newspolnes.view

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mxlkt.newspolnes.api.ApiClient
import com.mxlkt.newspolnes.model.ErrorResponse
import com.mxlkt.newspolnes.model.LoginRequest
import com.mxlkt.newspolnes.model.RegisterRequest
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.User
import com.mxlkt.newspolnes.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

// Sealed class untuk mengelola state UI Otentikasi
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class SuccessLogin(val user: User) : AuthState()
    data class SuccessRegister(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel yang bertanggung jawab HANYA untuk operasi Autentikasi dan pengelolaan sesi.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.apiService
    private val sessionManager = SessionManager(application)

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    // --- Data Sesi dari DataStore (Flow) ---
    val userName = sessionManager.userName
    val userEmail = sessionManager.userEmail
    val userId = sessionManager.userId
    val isLoggedIn = sessionManager.isLoggedIn
    val userRole = sessionManager.userRole

    suspend fun checkLoginStatus(): Boolean {
        return isLoggedIn.first()
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email dan password tidak boleh kosong.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Perhatikan: LoginRequest yang Anda berikan di model menggunakan 'name' dan 'password',
                // tetapi di fungsi ini Anda menggunakan 'email' dan 'password'.
                // Saya menggunakan 'email' sebagai argumen pertama agar konsisten dengan panggilan fungsi,
                // asumsi di balik layar API menerima 'email' meskipun model request bernama 'name'.
                // Jika API benar-benar mengharapkan 'name', ganti 'email' dengan 'name' di sini.
                val response = apiService.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val user = response.body()!!.data

                    sessionManager.saveLoginSession(
                        isLoggedIn = true,
                        userId = user.id,
                        userName = user.name,
                        userEmail = user.email,
                        userRole = user.role.name
                    )
                    _authState.value = AuthState.SuccessLogin(user)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Gagal Login. Periksa kredensial Anda."
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: IOException) {
                _authState.value = AuthState.Error("Kesalahan jaringan: Periksa koneksi internet Anda.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Terjadi kesalahan tak terduga: ${e.message}")
            }
        }
    }

// ... (Bagian import dan class AuthViewModel)

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Semua kolom harus diisi.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password minimal 6 karakter.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Pastikan apiService.register menggunakan Response<RegisterResponse>
                val response = apiService.register(RegisterRequest(name, email, password))

                if (response.isSuccessful) {
                    val registerResponse = response.body() // Ambil body

                    // 1. Cek apakah body tidak null DAN properti 'user' ada
                    val user: User? = registerResponse?.user

                    if (user != null) {
                        // 2. KOREKSI: Pastikan userRole yang disimpan adalah String
                        // Jika user.role sudah String, ini sudah benar.
                        // Jika role adalah Enum, Anda perlu .name (misalnya: user.role.name)
                        StoreData._userList.add(user) // Mengakses langsung untuk penambahan instan
                        sessionManager.saveLoginSession(
                            isLoggedIn = true,
                            userId = user.id,
                            userName = user.name,
                            userEmail = user.email,
                            userRole = user.role.toString() // Asumsi user.role adalah String
                        )
                        _authState.value = AuthState.SuccessRegister(user)
                    } else {
                        // Jika 201 tapi body user kosong (Error Parsing/Server Error)
                        _authState.value = AuthState.Error("Registrasi berhasil, tetapi respons data tidak valid.")
                    }

                } else {
                    // Penanganan respons GAGAL (misalnya 400 Bad Request)
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        // Coba parse ErrorResponse dari body error
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        // Jika gagal parse, gunakan pesan default
                        "Registrasi gagal: Kode ${response.code()}."
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }

            } catch (e: IOException) {
                // Kesalahan jaringan/koneksi
                _authState.value = AuthState.Error("Kesalahan jaringan: Periksa koneksi internet Anda.")
            } catch (e: Exception) {
                // Kesalahan lain (misalnya, masalah parsing)
                _authState.value = AuthState.Error("Terjadi kesalahan tak terduga: ${e.message}")
                e.printStackTrace() // Penting untuk debugging!
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _authState.value = AuthState.Idle
        }
    }
}
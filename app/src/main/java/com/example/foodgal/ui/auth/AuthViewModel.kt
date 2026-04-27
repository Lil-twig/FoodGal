package com.example.foodgal.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun signIn(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isEmpty() || pass.isEmpty()) {
            _error.value = "Email dan password tidak boleh kosong"
            return
        }
        _isLoading.value = true
        _error.value = null
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser
                    onSuccess()
                } else {
                    _error.value = task.exception?.message ?: "Login gagal"
                }
            }
    }

    fun signUp(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isEmpty() || pass.isEmpty()) {
            _error.value = "Email dan password tidak boleh kosong"
            return
        }
        _isLoading.value = true
        _error.value = null
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser
                    onSuccess()
                } else {
                    _error.value = task.exception?.message ?: "Pendaftaran gagal"
                }
            }
    }

    fun signOut(onSuccess: () -> Unit) {
        auth.signOut()
        _currentUser.value = null
        onSuccess()
    }

    fun clearError() {
        _error.value = null
    }
}

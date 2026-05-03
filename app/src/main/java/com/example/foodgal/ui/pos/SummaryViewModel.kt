package com.example.foodgal.ui.pos

import androidx.lifecycle.ViewModel
import com.example.foodgal.models.Transaction
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SummaryViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val _totalSales = MutableStateFlow(0.0)
    val totalSales: StateFlow<Double> = _totalSales.asStateFlow()

    private val _totalProfit = MutableStateFlow(0.0)
    val totalProfit: StateFlow<Double> = _totalProfit.asStateFlow()

    private val _transactionCount = MutableStateFlow(0)
    val transactionCount: StateFlow<Int> = _transactionCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        calculateSummary()
    }

    private fun calculateSummary() {
        _isLoading.value = true
        db.collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val transactions = snapshot.toObjects(Transaction::class.java)
                    var sales = 0.0
                    transactions.forEach { sales += it.totalAmount }
                    
                    _totalSales.value = sales
                    _transactionCount.value = transactions.size
                    // Estimasi keuntungan 20% (karena data modal/cost belum ada)
                    _totalProfit.value = sales * 0.2
                }
                _isLoading.value = false
            }
    }
}

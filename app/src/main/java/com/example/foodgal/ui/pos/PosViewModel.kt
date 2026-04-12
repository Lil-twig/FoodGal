package com.example.foodgal.ui.pos

import androidx.lifecycle.ViewModel
import com.example.foodgal.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class PosViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _filteredProducts.asStateFlow()

    // CART STATE
    private val _cartItems = MutableStateFlow<Map<String, Int>>(emptyMap()) // productId to quantity
    val cartItems: StateFlow<Map<String, Int>> = _cartItems.asStateFlow()

    val cartItemCount: StateFlow<Int> = _cartItems.map { it.values.sum() }.let { 
        val flow = MutableStateFlow(0)
        // Note: Simple mapping for state
        it
    }.let { 
        // Real implementation using stateIn would be better, but we'll use a derived flow
        _cartItems.map { it.values.sum() }
    }.let { flow ->
        val result = MutableStateFlow(0)
        // This is a bit hacky without viewModelScope, let's just use manual updates for now to be safe
        MutableStateFlow(0) 
    }
    
    // Let's use simpler state for cart summary
    private val _totalItems = MutableStateFlow(0)
    val totalItems: StateFlow<Int> = _totalItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0)
    val totalPrice: StateFlow<Int> = _totalPrice.asStateFlow()

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        _isLoading.value = true
        repository.fetchProducts(
            onSuccess = { products ->
                _allProducts.value = products
                updateFilteredList()
                _isLoading.value = false
            },
            onFailure = {
                _isLoading.value = false
            }
        )
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        updateFilteredList()
    }

    private fun updateFilteredList() {
        val currentCategory = _selectedCategory.value
        if (currentCategory == "All") {
            _filteredProducts.value = _allProducts.value
            return
        }
        _filteredProducts.value = _allProducts.value.filter { 
            it.category.equals(currentCategory, ignoreCase = true) 
        }
    }

    fun addToCart(product: Product) {
        val currentCart = _cartItems.value.toMutableMap()
        val count = currentCart.getOrDefault(product.id, 0)
        currentCart[product.id] = count + 1
        _cartItems.value = currentCart
        
        // Update summary
        _totalItems.value = currentCart.values.sum()
        _totalPrice.value += product.price.toInt()
    }
    
    fun removeFromCart(product: Product) {
        val currentCart = _cartItems.value.toMutableMap()
        val count = currentCart.getOrDefault(product.id, 0)
        if (count > 0) {
            if (count == 1) {
                currentCart.remove(product.id)
            } else {
                currentCart[product.id] = count - 1
            }
            _cartItems.value = currentCart
            _totalItems.value = currentCart.values.sum()
            _totalPrice.value -= product.price.toInt()
        }
    }
    
    fun isProductInCart(productId: String): Boolean {
        return _cartItems.value.containsKey(productId)
    }
}

package com.onserver1.app.ui.screens.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onserver1.app.data.model.CreateOrderItem
import com.onserver1.app.data.model.Order
import com.onserver1.app.data.model.Product
import com.onserver1.app.data.model.ProductField
import com.onserver1.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val fields: List<ProductField> = emptyList(),
    val fieldValues: MutableMap<String, String> = mutableMapOf(),
    val imei: String = "",
    val quantity: Int = 1,
    val isOrdering: Boolean = false,
    val orderSuccess: Boolean = false,
    val orderResult: Order? = null,
    val error: String? = null,
    val userBalance: Double = 0.0
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailUiState())
    val state: StateFlow<ProductDetailUiState> = _state

    private val productId: String = savedStateHandle["productId"] ?: ""

    init {
        loadProduct()
        loadUserBalance()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = productRepository.getProduct(productId)
            result.onSuccess { product ->
                // Parse fields JSON
                val fields = parseFields(product.fields)
                _state.value = _state.value.copy(
                    product = product,
                    fields = fields,
                    isLoading = false,
                    quantity = if (product.minQnt > 0) product.minQnt else 1
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(error = error.message, isLoading = false)
            }
        }
    }

    private fun loadUserBalance() {
        viewModelScope.launch {
            productRepository.getProfile().onSuccess { user ->
                _state.value = _state.value.copy(userBalance = user.balance)
            }
        }
    }

    private fun parseFields(fieldsJson: String?): List<ProductField> {
        if (fieldsJson.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<ProductField>>() {}.type
            Gson().fromJson<List<ProductField>>(fieldsJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun updateFieldValue(key: String, value: String) {
        val newValues = _state.value.fieldValues.toMutableMap()
        newValues[key] = value
        _state.value = _state.value.copy(fieldValues = newValues)
    }

    fun updateImei(imei: String) {
        _state.value = _state.value.copy(imei = imei)
    }

    fun updateQuantity(qty: Int) {
        val product = _state.value.product ?: return
        val min = if (product.minQnt > 0) product.minQnt else 1
        val max = if (product.maxQnt > 0) product.maxQnt else 999
        val clamped = qty.coerceIn(min, max)
        _state.value = _state.value.copy(quantity = clamped)
    }

    fun placeOrder() {
        val product = _state.value.product ?: return
        val state = _state.value

        // Validate required fields from backend
        for (field in state.fields) {
            if (field.required && (state.fieldValues[field.key].isNullOrBlank())) {
                _state.value = _state.value.copy(error = "Please fill in: ${field.name}")
                return
            }
        }

        _state.value = _state.value.copy(isOrdering = true, error = null)

        viewModelScope.launch {
            val metadata = mutableMapOf<String, Any>()
            // Extract IMEI from fieldValues: check if any key contains "imei", "lock code", or "sn"
            val imeiEntry = state.fieldValues.entries.find { entry ->
                val k = entry.key.lowercase()
                k.contains("imei") || k.contains("lock code") || k.contains("sn")
            }
            // For IMEI service type, use first field value as fallback
            val imeiValue = imeiEntry?.value?.takeIf { it.isNotBlank() }
                ?: if (product.serviceType == "IMEI") state.fieldValues.values.firstOrNull() else null
            if (!imeiValue.isNullOrBlank()) {
                metadata["imei"] = imeiValue
            }
            if (state.fieldValues.isNotEmpty()) {
                metadata["fieldValues"] = state.fieldValues.toMap()
            }

            @Suppress("UNCHECKED_CAST")
            val item = CreateOrderItem(
                productId = product.id,
                quantity = state.quantity,
                metadata = metadata as? Map<String, String>
            )

            val result = productRepository.createOrder(listOf(item))
            result.onSuccess { order ->
                _state.value = _state.value.copy(
                    isOrdering = false,
                    orderSuccess = true,
                    orderResult = order
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isOrdering = false,
                    error = error.message
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

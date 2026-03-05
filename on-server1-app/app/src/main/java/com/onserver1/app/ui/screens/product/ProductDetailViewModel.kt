package com.onserver1.app.ui.screens.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                val fields = product.fields ?: emptyList()
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

    fun updateFieldValue(key: String, value: String) {
        // Determine max length based on field name/type
        val field = _state.value.fields.find { it.key == key }
        val maxLen = getFieldMaxLength(field)
        val trimmed = if (maxLen > 0 && value.length > maxLen) value.take(maxLen) else value

        val newValues = _state.value.fieldValues.toMutableMap()
        newValues[key] = trimmed
        _state.value = _state.value.copy(fieldValues = newValues)
    }

    /** Returns the max character limit for a field based on its name/type */
    fun getFieldMaxLength(field: ProductField?): Int {
        if (field == null) return 0
        val k = field.key.lowercase()
        val n = field.name.lowercase()
        return when {
            k.contains("imei") || n.contains("imei") -> 20
            k.contains("sn") || n.contains("serial") || n.contains("sn") -> 20
            k.contains("teamviewer") || n.contains("teamviewer") -> 15
            k.contains("password") || n.contains("password") -> 30
            field.type.equals("NUMBER", true) -> 20
            field.type.equals("TEXTAREA", true) -> 500
            else -> 100
        }
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
            val finalFieldValues = state.fieldValues.toMutableMap()
            // Extract IMEI from fieldValues
            val imeiEntry = finalFieldValues.entries.find { entry ->
                val k = entry.key.lowercase()
                k.contains("imei") || k.contains("lock code") || k.contains("sn")
            }
            val imeiValue = imeiEntry?.value?.takeIf { it.isNotBlank() }
                ?: if (product.serviceType == "IMEI") finalFieldValues.values.firstOrNull() else null
            if (!imeiValue.isNullOrBlank()) {
                metadata["imei"] = imeiValue
            }
            if (finalFieldValues.isNotEmpty()) {
                metadata["fieldValues"] = finalFieldValues.toMap()
            }

            val item = CreateOrderItem(
                productId = product.id,
                quantity = state.quantity,
                metadata = metadata
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

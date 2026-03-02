package com.onserver1.app.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onserver1.app.data.model.Product
import com.onserver1.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GamesUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val groups: List<String> = emptyList(),
    val selectedGroup: String? = null,
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GamesUiState())
    val state: StateFlow<GamesUiState> = _state

    init {
        loadGroups()
        loadProducts()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            val result = productRepository.getGroups("IMEI")
            result.onSuccess { groups ->
                _state.value = _state.value.copy(groups = groups)
            }
        }
    }

    fun selectGroup(group: String?) {
        _state.value = _state.value.copy(selectedGroup = group)
        loadProducts()
    }

    fun searchProducts(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val s = _state.value
            val result = productRepository.getProducts(
                serviceType = "IMEI",
                groupName = s.selectedGroup,
                search = s.searchQuery.ifBlank { null }
            )
            result.onSuccess { products ->
                _state.value = _state.value.copy(products = products, isLoading = false, error = null)
            }.onFailure { error ->
                _state.value = _state.value.copy(error = error.message, isLoading = false)
            }
        }
    }
}

package com.onserver1.app.ui.screens.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onserver1.app.data.model.Category
import com.onserver1.app.data.model.Product
import com.onserver1.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServicesUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val groups: List<String> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedGroup: String? = null,
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ServicesUiState())
    val state: StateFlow<ServicesUiState> = _state

    init {
        loadGroups()
        loadCategories()
        loadProducts()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            val result = productRepository.getGroups("SERVER")
            result.onSuccess { groups ->
                _state.value = _state.value.copy(groups = groups)
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categoriesResult = productRepository.getCategories()
            categoriesResult.onSuccess { categories ->
                _state.value = _state.value.copy(categories = categories)
            }
        }
    }

    fun selectGroup(group: String?) {
        _state.value = _state.value.copy(selectedGroup = group)
        loadProducts()
    }

    fun selectCategory(categoryId: String?) {
        _state.value = _state.value.copy(selectedCategoryId = categoryId)
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
                categoryId = s.selectedCategoryId,
                serviceType = "SERVER",
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

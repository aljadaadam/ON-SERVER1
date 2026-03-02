package com.onserver1.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onserver1.app.data.model.Banner
import com.onserver1.app.data.model.Product
import com.onserver1.app.data.model.User
import com.onserver1.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val banners: List<Banner> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Load profile
            val profileResult = productRepository.getProfile()
            profileResult.onSuccess { user ->
                _state.value = _state.value.copy(user = user)
            }

            // Load banners
            val bannersResult = productRepository.getBanners()
            bannersResult.onSuccess { banners ->
                _state.value = _state.value.copy(banners = banners)
            }

            // Load featured products
            val productsResult = productRepository.getFeaturedProducts()
            productsResult.onSuccess { products ->
                _state.value = _state.value.copy(featuredProducts = products)
            }.onFailure { error ->
                _state.value = _state.value.copy(error = error.message)
            }

            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun refreshBalance() {
        viewModelScope.launch {
            val profileResult = productRepository.getProfile()
            profileResult.onSuccess { user ->
                _state.value = _state.value.copy(user = user)
            }
        }
    }
}

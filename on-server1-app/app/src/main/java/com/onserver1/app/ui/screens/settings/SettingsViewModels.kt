package com.onserver1.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onserver1.app.data.model.User
import com.onserver1.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val success: String? = null,
    val error: String? = null
)

data class ChangePasswordUiState(
    val isSaving: Boolean = false,
    val success: String? = null,
    val error: String? = null
)

data class AppSettingsUiState(
    val settings: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val result = productRepository.getProfile()
            result.onSuccess { user ->
                _state.value = EditProfileUiState(user = user, isLoading = false)
            }.onFailure {
                _state.value = EditProfileUiState(isLoading = false, error = it.message)
            }
        }
    }

    fun updateProfile(name: String, phone: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null, success = null)
            val result = productRepository.updateProfile(name, phone)
            result.onSuccess { user ->
                _state.value = _state.value.copy(user = user, isSaving = false, success = "Profile updated")
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(success = null, error = null)
    }
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _state.value = ChangePasswordUiState(isSaving = true)
            val result = productRepository.changePassword(currentPassword, newPassword)
            result.onSuccess {
                _state.value = ChangePasswordUiState(success = it)
            }.onFailure {
                _state.value = ChangePasswordUiState(error = it.message)
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(success = null, error = null)
    }
}

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppSettingsUiState())
    val state: StateFlow<AppSettingsUiState> = _state

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = productRepository.getAppSettings()
            result.onSuccess { settings ->
                _state.value = AppSettingsUiState(settings = settings, isLoading = false)
            }.onFailure {
                _state.value = AppSettingsUiState(isLoading = false, error = it.message)
            }
        }
    }
}

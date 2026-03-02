package com.onserver1.app.ui.screens.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onserver1.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _isMaintenanceMode = MutableStateFlow(false)
    val isMaintenanceMode: StateFlow<Boolean> = _isMaintenanceMode

    private val _isChecking = MutableStateFlow(true)
    val isChecking: StateFlow<Boolean> = _isChecking

    init {
        checkMaintenanceMode()
    }

    fun checkMaintenanceMode() {
        viewModelScope.launch {
            _isChecking.value = true
            try {
                val result = productRepository.getAppSettings()
                result.onSuccess { settings ->
                    _isMaintenanceMode.value = settings["maintenance_mode"] == "true"
                }.onFailure {
                    // If we can't reach server, don't block the app
                    _isMaintenanceMode.value = false
                }
            } catch (_: Exception) {
                _isMaintenanceMode.value = false
            }
            _isChecking.value = false
        }
    }
}

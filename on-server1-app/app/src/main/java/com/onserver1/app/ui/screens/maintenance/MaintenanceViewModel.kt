package com.onserver1.app.ui.screens.maintenance

import android.util.Log
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
                Log.d("MaintenanceVM", "Checking maintenance mode...")
                val result = productRepository.getAppSettings()
                result.onSuccess { settings ->
                    val mode = settings["maintenance_mode"] == "true"
                    Log.d("MaintenanceVM", "maintenance_mode value from server: '${settings["maintenance_mode"]}' -> isMaintenanceMode=$mode")
                    _isMaintenanceMode.value = mode
                }.onFailure {
                    Log.e("MaintenanceVM", "Failed to get settings: ${it.message}")
                    // If we can't reach server, don't block the app
                    _isMaintenanceMode.value = false
                }
            } catch (e: Exception) {
                Log.e("MaintenanceVM", "Exception checking maintenance: ${e.message}")
                _isMaintenanceMode.value = false
            }
            _isChecking.value = false
        }
    }
}

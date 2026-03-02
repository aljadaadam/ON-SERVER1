package com.onserver1.app.ui.screens.deposit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onserver1.app.data.model.*
import com.onserver1.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================
// Gateway Info ViewModel
// ============================================
data class DepositState(
    val isLoading: Boolean = false,
    val gatewayInfo: GatewayInfo? = null,
    val error: String? = null,
    val depositResult: Deposit? = null,
    val usdtResult: DepositResponse? = null,
    val isSubmitting: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DepositState())
    val state: StateFlow<DepositState> = _state.asStateFlow()

    init {
        loadGatewayInfo()
    }

    fun loadGatewayInfo() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getGatewayInfo().fold(
                onSuccess = { info ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        gatewayInfo = info
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun submitUsdtDeposit(amount: Double, txHash: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null, successMessage = null)
            repository.createUsdtDeposit(amount, txHash).fold(
                onSuccess = { result ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        usdtResult = result,
                        successMessage = if (result.verification?.verified == true)
                            "تم التحقق وإضافة الرصيد بنجاح"
                        else
                            "تم إرسال الطلب وسيتم مراجعته"
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun submitBankakDeposit(amount: Double, receiptFile: java.io.File, note: String? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null, successMessage = null)
            repository.createBankakDeposit(amount, receiptFile, note).fold(
                onSuccess = { deposit ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        depositResult = deposit,
                        successMessage = "تم إرسال طلب الإيداع #${deposit.depositNumber} بنجاح\nسيتم مراجعته من قبل الإدارة"
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
}

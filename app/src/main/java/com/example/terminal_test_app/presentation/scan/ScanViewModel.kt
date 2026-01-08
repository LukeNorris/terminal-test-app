package com.example.terminal_test_app.presentation.scan


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.terminal_test_app.domain.model.ScanMethod
import com.example.terminal_test_app.domain.model.ScanResult
import com.example.terminal_test_app.domain.usecase.ScanCodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScanViewModel(
    private val scanCodeUseCase: ScanCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState

    fun setScanType(type: ScanMethod) {
        _uiState.update {
            it.copy(
                scanType = type,
                result = null,
                isScanning = false
            )
        }
    }

    fun startScan() {
        _uiState.update {
            it.copy(
                isScanning = true,
                result = null
            )
        }
    }

    fun onQrScanned(value: String) {
        _uiState.update {
            it.copy(
                result = ScanResult.QrCode(value),
                isScanning = false
            )
        }
    }

    fun reset() {
        _uiState.value = ScanUiState()
    }
}

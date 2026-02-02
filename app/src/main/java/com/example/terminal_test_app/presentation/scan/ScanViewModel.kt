package com.example.terminal_test_app.presentation.scan


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.terminal_test_app.domain.model.ScanMethod
import com.example.terminal_test_app.domain.model.ScanResult
import com.example.terminal_test_app.domain.usecase.ScanBarcodeUseCase
import com.example.terminal_test_app.domain.usecase.ScanCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/*class ScanViewModel(
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
}*/

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanBarcodeUseCase: ScanBarcodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState

    private var currentSessionId: String? = null

    fun setScanType(type: ScanMethod) {
        _uiState.update {
            it.copy(scanType = type, result = null, isScanning = false)
        }
    }

    fun startScan() {
        when (_uiState.value.scanType) {
            ScanMethod.SCAN_QR_CODE -> {
                _uiState.update { it.copy(isScanning = true) }
            }

            ScanMethod.SCAN_BAR_CODE -> {
                startTerminalBarcodeScan()
            }
        }
    }

    fun cancelScan() {
        _uiState.update {
            it.copy(
                isScanning = false,
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

    private fun startTerminalBarcodeScan() {
        val sessionId = UUID.randomUUID().toString().take(8)
        currentSessionId = sessionId

        _uiState.update { state ->
            state.copy(
                isScanning = true,
                debugMessage = "Waiting for terminal response…"
            )
        }

        viewModelScope.launch {
            val result = scanBarcodeUseCase(
                sessionId = sessionId,
                timeoutMs = 5000
            )

            _uiState.update { state ->
                result.fold(
                    onSuccess = { barcode ->
                        state.copy(
                            isScanning = false,
                            result = ScanResult.BarCode(barcode.data),
                            debugMessage = "SUCCESS: ${barcode.data}"
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isScanning = false,
                            result = ScanResult.Cancelled,
                            debugMessage = error.message ?: "Scan failed"
                        )
                    }
                )
            }
        }
    }



    fun reset() {
        _uiState.value = ScanUiState()
    }
}

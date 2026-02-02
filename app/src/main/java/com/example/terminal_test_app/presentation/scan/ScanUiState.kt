package com.example.terminal_test_app.presentation.scan

import com.example.terminal_test_app.domain.model.ScanResult
import com.example.terminal_test_app.domain.model.ScanMethod

data class ScanUiState(
    val scanType: ScanMethod = ScanMethod.SCAN_QR_CODE,
    val isScanning: Boolean = false,
    val result: ScanResult? = null,
    val debugMessage: String? = null
)

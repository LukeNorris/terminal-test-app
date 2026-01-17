package com.example.terminal_test_app.domain.usecase

import com.example.terminal_test_app.domain.model.BarcodeScanResult
import com.example.terminal_test_app.domain.repository.BarcodeScanner
import javax.inject.Inject

class ScanBarcodeUseCase @Inject constructor(
    private val repository: BarcodeScanner
) {
    suspend operator fun invoke(sessionId: String, timeoutMs: Int): Result<BarcodeScanResult> {
        return repository.scanSingleBarcode(sessionId, timeoutMs)
    }
}
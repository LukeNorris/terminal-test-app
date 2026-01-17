package com.example.terminal_test_app.domain.usecase


import com.example.terminal_test_app.domain.repository.BarcodeScanner
import javax.inject.Inject

class CancelBarcodeScanUseCase @Inject constructor(
    private val repository: BarcodeScanner
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        return repository.cancelBarcodeScan(sessionId)
    }
}
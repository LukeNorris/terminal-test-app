package com.example.terminal_test_app.domain.usecase

import com.example.terminal_test_app.domain.model.ScanMethod
import com.example.terminal_test_app.domain.model.ScanResult
import com.example.terminal_test_app.domain.repository.CodeScanner

class ScanCodeUseCase(
    val codeScanner: CodeScanner
) {
    suspend operator fun invoke(
        scanMethod: ScanMethod
    ): ScanResult{
        return when (scanMethod) {
            ScanMethod.SCAN_BAR_CODE -> codeScanner.scanBarCode()
            ScanMethod.SCAN_QR_CODE -> codeScanner.scanQrCode()
        }
    }
}
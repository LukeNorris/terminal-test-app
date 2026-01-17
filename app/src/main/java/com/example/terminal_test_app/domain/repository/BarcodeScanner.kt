package com.example.terminal_test_app.domain.repository

import com.example.terminal_test_app.domain.model.BarcodeScanResult

interface BarcodeScanner {
    suspend fun scanSingleBarcode(sessionId: String, timeoutMs: Int): Result<BarcodeScanResult>
    suspend fun cancelBarcodeScan(sessionId: String): Result<Unit>  // For canceling before timeout
}
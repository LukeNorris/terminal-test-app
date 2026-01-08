package com.example.terminal_test_app.domain.repository

import com.example.terminal_test_app.domain.model.ScanResult

interface CodeScanner {
    suspend fun scanQrCode(): ScanResult
    suspend fun scanBarCode(): ScanResult
}
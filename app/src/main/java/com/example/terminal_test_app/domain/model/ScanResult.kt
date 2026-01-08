package com.example.terminal_test_app.domain.model

sealed class ScanResult {
    data class QrCode(
        val rawValue: String
    ) : ScanResult()

    data class BarCode(
        val rawValue: String
    ) : ScanResult()

    object Cancelled : ScanResult()

}
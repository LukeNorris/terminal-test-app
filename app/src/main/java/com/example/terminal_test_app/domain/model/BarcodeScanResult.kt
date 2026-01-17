package com.example.terminal_test_app.domain.model

data class BarcodeScanResult(
    val data: String,          // The scanned content (e.g., "8600197416824")
    val symbology: String      // Format (e.g., "QR_CODE" or "UNKNOWN")
)
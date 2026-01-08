// platform/scan/AndroidCodeScanner.kt
package com.example.terminal_test_app.platform.scan

import android.hardware.Sensor
import androidx.compose.ui.platform.LocalContext
import com.example.terminal_test_app.domain.model.ScanResult
import com.example.terminal_test_app.domain.repository.CodeScanner
import kotlinx.coroutines.delay

class AndroidCodeScanner : CodeScanner {


    override suspend fun scanQrCode(): ScanResult {
        // Simulate camera delay
        delay(1_000)
        return ScanResult.QrCode(
            rawValue = "https://example.com/qr-result"
        )
    }

    override suspend fun scanBarCode(): ScanResult {
        delay(1_000)
        return ScanResult.BarCode(
            rawValue = "1234567890"
        )
    }
}




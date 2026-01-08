package com.example.terminal_test_app.platform.scan

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QrAnalyzer(
    private val onQrScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private var hasScanned = false

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        if (hasScanned) {
            imageProxy.close()
            return
        }

        val image = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val input = InputImage.fromMediaImage(
            image,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { qr ->
                    hasScanned = true
                    onQrScanned(qr)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

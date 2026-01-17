package com.example.terminal_test_app.data.mapper

import com.example.terminal_test_app.data.remote.dto.SaleToPOIResponse
import com.example.terminal_test_app.domain.model.BarcodeScanResult
import java.util.Base64
import org.json.JSONObject

fun SaleToPOIResponse.toDomain(): Result<BarcodeScanResult> {
    val response = this.AdminResponse?.Response ?: return Result.failure(Exception("No AdminResponse"))

    return when (response.Result) {
        "Success" -> {
            val additional = this.AdminResponse.AdditionalResponse
                ?: response.AdditionalResponse
                ?: return Result.failure(Exception("No AdditionalResponse"))

            try {
                val decoded = String(Base64.getDecoder().decode(additional))
                val json = JSONObject(decoded)
                val barcodeObj = json.optJSONObject("Barcode")
                    ?: return Result.failure(Exception("No Barcode object"))

                BarcodeScanResult(
                    data = barcodeObj.optString("Data", ""),
                    symbology = barcodeObj.optString("Symbology", "UNKNOWN")
                ).let { Result.success(it) }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        "Failure" -> {
            val errorMsg = response.ErrorCondition ?: "Unknown error"
            val additional = this.AdminResponse.AdditionalResponse
                ?: response.AdditionalResponse
            val decodedMsg = additional?.let { String(Base64.getDecoder().decode(it)) } ?: ""
            Result.failure(Exception("$errorMsg - $decodedMsg"))
        }
        else -> Result.failure(Exception("Unknown result: ${response.Result}"))
    }
}
package com.example.terminal_test_app.data.repository


import com.example.terminal_test_app.data.mapper.toDomain
import com.example.terminal_test_app.data.remote.api.TerminalApi
import com.example.terminal_test_app.data.remote.dto.*
import com.example.terminal_test_app.domain.model.BarcodeScanResult
import com.example.terminal_test_app.domain.repository.BarcodeScanner
import java.util.Base64
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class TerminalRepositoryImpl @Inject constructor(
    private val api: TerminalApi,
    private val saleId: String,              // Your POS ID
    private val poiId: String                // e.g., "S1EL-324688179" - terminal ID
) : BarcodeScanner {

    override suspend fun scanSingleBarcode(sessionId: String, timeoutMs: Int): Result<BarcodeScanResult> {
        val innerJson = ScanSessionJson(
            Session(Id = sessionId, Type = "Once"),
            Operation = listOf(Operation(TimeoutMs = timeoutMs.coerceIn(0, 30000)))
        )

        val jsonString = Json.encodeToString(ScanSessionJson.serializer(), innerJson)
        val base64 = Base64.getEncoder().encodeToString(jsonString.toByteArray())

        val request = SaleToPOIRequest(
            MessageHeader = MessageHeader(
                ServiceID = UUID.randomUUID().toString().take(10),  // Unique per request
                SaleID = saleId,
                POIID = poiId
            ),
            AdminRequest = AdminRequest(ServiceIdentification = base64)
        )

        return try {
            val response = api.sendAdminRequest(request)
            response.toDomain()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelBarcodeScan(sessionId: String): Result<Unit> {
        val innerJson = mapOf("Session" to mapOf("Id" to sessionId, "Type" to "End"))
        val jsonString = kotlinx.serialization.json.Json.encodeToString(innerJson)
        val base64 = Base64.getEncoder().encodeToString(jsonString.toByteArray())

        val request = SaleToPOIRequest(
            MessageHeader = MessageHeader(
                ServiceID = UUID.randomUUID().toString().take(10),
                SaleID = saleId,
                POIID = poiId
            ),
            AdminRequest = AdminRequest(ServiceIdentification = base64)
        )

        return try {
            val response = api.sendAdminRequest(request)
            if (response.AdminResponse?.Response?.Result == "Success") {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Cancel failed: ${response.AdminResponse?.Response?.ErrorCondition}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
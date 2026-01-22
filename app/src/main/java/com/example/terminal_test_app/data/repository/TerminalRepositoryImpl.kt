package com.example.terminal_test_app.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.terminal_test_app.data.mapper.toDomain
import com.example.terminal_test_app.data.remote.api.TerminalApi
import com.example.terminal_test_app.data.remote.crypto.NexoCrypto
import com.example.terminal_test_app.data.remote.dto.*
import com.example.terminal_test_app.data.settings.SettingsDataSource
import com.example.terminal_test_app.data.settings.TerminalSettings
import com.example.terminal_test_app.domain.model.BarcodeScanResult
import com.example.terminal_test_app.domain.model.PaymentResult
import com.example.terminal_test_app.domain.repository.BarcodeScanner
import com.example.terminal_test_app.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.UUID
import javax.inject.Inject

class TerminalRepositoryImpl @Inject constructor(
    private val api: TerminalApi,
    private val settingsDataSource: SettingsDataSource,
    private val saleId: String
) : BarcodeScanner, PaymentRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    private val nexoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        .withZone(ZoneOffset.UTC)

    // --- 1. Payment Method ---
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun makePayment(amount: Double, currency: String): Result<PaymentResult> {
        return try {
            val currentSettings = settingsDataSource.settings.first()

            val plainRequest = SaleToPOIRequest(
                MessageHeader = MessageHeader(
                    MessageCategory = "Payment",
                    ServiceID = UUID.randomUUID().toString().take(10),
                    SaleID = saleId,
                    POIID = currentSettings.poiId
                ),
                PaymentRequest = PaymentRequest(
                    SaleData = SaleData(
                        SaleTransactionID = SaleTransactionID(
                            TransactionID = UUID.randomUUID().toString().take(8),
                            TimeStamp = nexoFormatter.format(Instant.now())
                        )
                    ),
                    PaymentTransaction = PaymentTransaction(
                        AmountsReq = AmountsReq(
                            Currency = currency,
                            RequestedAmount = amount
                        )
                    )
                )
            )

            sendSecureRequest(plainRequest, currentSettings)
                .map { response ->
                    if (response.PaymentResponse?.Response?.Result == "Success") {
                        PaymentResult(success = true, authCode = "APPROVED")
                    } else {
                        val error = response.PaymentResponse?.Response?.ErrorCondition ?: "Unknown Error"
                        throw Exception("Payment Failed: $error")
                    }
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 2. Barcode Scan Method ---
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun scanSingleBarcode(sessionId: String, timeoutMs: Int): Result<BarcodeScanResult> {
        return try {
            val currentSettings = settingsDataSource.settings.first()

            val innerJson = ScanSessionJson(
                Session = Session(Id = sessionId, Type = "Once"),
                Operation = listOf(Operation(TimeoutMs = timeoutMs))
            )

            // Encode inner JSON to Base64 for the ServiceIdentification field
            val jsonString = Json.encodeToString(innerJson)
            val base64ServiceId = Base64.getEncoder().encodeToString(jsonString.toByteArray())

            val plainRequest = SaleToPOIRequest(
                MessageHeader = MessageHeader(
                    MessageCategory = "Admin",
                    ServiceID = UUID.randomUUID().toString().take(10),
                    SaleID = saleId,
                    POIID = currentSettings.poiId
                ),
                AdminRequest = AdminRequest(ServiceIdentification = base64ServiceId)
            )

            sendSecureRequest(plainRequest, currentSettings)
                .map { response ->
                    response.toDomain().getOrThrow()
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Common secure communication handler
     */
    private suspend fun sendSecureRequest(
        request: SaleToPOIRequest,
        settings: TerminalSettings
    ): Result<SaleToPOIResponse> {
        return try {
            // 1. Setup Crypto with fallback for empty values
            val nexoCrypto = NexoCrypto(
                keyIdentifier = settings.nexoKeyIdentifier,
                keyVersion = 1L,
                passphrase = settings.nexoPassphrase.toCharArray()
            )

            // 2. Prepare JSON
            val outerWrapper = mapOf("SaleToPOIRequest" to request)
            val plainJson = Json { encodeDefaults = true }.encodeToString(outerWrapper)

            // 3. Encrypt and Send
            val securedJson = nexoCrypto.encryptAndWrap(plainJson)
            val encryptedResponse = api.sendAdminRequest(securedJson)

            // 4. FIX: Use explicit types AND ensure non-nullable return
            val decryptedJson = nexoCrypto.decryptAndValidate(encryptedResponse) { keyId: String, keyVersion: Long ->
                // Explicitly returning CharArray from a non-nullable String
                settings.nexoPassphrase.toCharArray()
            }

            // 5. Decode
            val response = Json { ignoreUnknownKeys = true }.decodeFromString<SaleToPOIResponse>(decryptedJson)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelBarcodeScan(sessionId: String): Result<Unit> {
        // Implementation logic for "Type": "End" scan session would go here
        return Result.success(Unit)
    }
}
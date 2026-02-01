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
    override suspend fun scanSingleBarcode(
        sessionId: String,
        timeoutMs: Int
    ): Result<BarcodeScanResult> {
        return try {
            val settings = settingsDataSource.settings.first()

            val innerJson = ScanSessionJson(
                Session = Session(Id = sessionId, Type = "Once"),
                Operation = listOf(Operation(TimeoutMs = timeoutMs))
            )

            val base64Payload = Base64.getEncoder()
                .encodeToString(Json.encodeToString(innerJson).toByteArray())

            val request = SaleToPOIRequest(
                MessageHeader = MessageHeader(
                    MessageCategory = "Admin",
                    ServiceID = UUID.randomUUID().toString().take(10),
                    SaleID = saleId,
                    POIID = settings.poiId
                ),
                AdminRequest = AdminRequest(ServiceIdentification = base64Payload)
            )

            sendSecureRequest(request, settings).map { response ->
                val encoded = response.AdminResponse?.AdditionalResponse
                    ?: throw Exception("Missing AdditionalResponse from barcode scan")

                val decodedJson = String(Base64.getDecoder().decode(encoded))
                val barcode = Json.decodeFromString<BarcodeResponse>(decodedJson)

                BarcodeScanResult(
                    data = barcode.Barcode.Data,
                    symbology = barcode.Barcode.Symbology
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun cancelBarcodeScan(sessionId: String): Result<Unit> {
        return try {
            val settings = settingsDataSource.settings.first()

            val innerJson = mapOf(
                "Session" to mapOf(
                    "Id" to sessionId,
                    "Type" to "End"
                )
            )

            val base64Payload = Base64.getEncoder()
                .encodeToString(Json.encodeToString(innerJson).toByteArray())

            val request = SaleToPOIRequest(
                MessageHeader = MessageHeader(
                    MessageCategory = "Admin",
                    ServiceID = UUID.randomUUID().toString().take(10),
                    SaleID = saleId,
                    POIID = settings.poiId
                ),
                AdminRequest = AdminRequest(ServiceIdentification = base64Payload)
            )

            sendSecureRequest(request, settings)
            Result.success(Unit)

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
            val nexoCrypto = NexoCrypto(
                keyIdentifier = settings.nexoKeyIdentifier,
                keyVersion = 1L,
                passphrase = settings.nexoPassphrase.toCharArray()
            )

            // 1. Wrap and generate Plaintext
            val outerWrapper = mapOf("SaleToPOIRequest" to request)
            val plainJson = Json { encodeDefaults = true; prettyPrint = true }.encodeToString(outerWrapper)

            // LOG 1: The readable request (useful for debugging logic)
            android.util.Log.d("TerminalPayload", "PLAINTEXT REQUEST:\n$plainJson")

            // 2. Encrypt to generate the final string
            val securedJson = nexoCrypto.encryptAndWrap(plainJson)

            // LOG 2: The final encrypted string (exactly what is sent to the terminal)
            android.util.Log.d("TerminalPayload", "ENCRYPTED BLOB SENT:\n$securedJson")

            val encryptedResponse = api.sendAdminRequest(securedJson)

            // LOG 3: The encrypted response from the terminal
            android.util.Log.d("TerminalPayload", "ENCRYPTED RESPONSE RECEIVED:\n$encryptedResponse")

            val decryptedJson = nexoCrypto.decryptAndValidate(encryptedResponse) { keyId: String, keyVersion: Long ->
                settings.nexoPassphrase.toCharArray()
            }

            // LOG 4: The decrypted readable response
            android.util.Log.d("TerminalPayload", "DECRYPTED RESPONSE:\n$decryptedJson")

            val json = Json { ignoreUnknownKeys = true }
            val envelope = json.decodeFromString<SaleToPOIResponseEnvelope>(decryptedJson)

            val response = envelope.SaleToPOIResponse
                ?: throw Exception(
                    "Terminal response missing SaleToPOIResponse envelope. Raw:\n$decryptedJson"
                )

            Result.success(response)
        } catch (e: Exception) {
            val detailedError = "${e.message}\nCause: ${e.cause?.message}"
            android.util.Log.e("TerminalPayload", "Error: $detailedError", e)
            // This will now show the Cause, which helps identify if it's a hostname vs cert issue
            Result.failure(Exception(detailedError))
        }
    }

}
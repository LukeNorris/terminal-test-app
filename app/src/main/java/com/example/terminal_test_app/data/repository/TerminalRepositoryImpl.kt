package com.example.terminal_test_app.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.terminal_test_app.data.remote.api.TerminalApi
import com.example.terminal_test_app.data.remote.crypto.NexoCrypto
import com.example.terminal_test_app.data.remote.dto.*
import com.example.terminal_test_app.data.settings.SettingsDataSource
import com.example.terminal_test_app.domain.model.BarcodeScanResult
import com.example.terminal_test_app.domain.model.PaymentResult
import com.example.terminal_test_app.domain.repository.BarcodeScanner
import com.example.terminal_test_app.domain.repository.PaymentRepository
import com.example.terminal_test_app.data.settings.TerminalSettings
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import android.net.Uri
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

            // 1. Build inner ScanBarcode JSON
            val innerJson = ScanSessionJson(
                Session = Session(
                    Id = sessionId,
                    Type = "Once"
                ),
                Operation = listOf(
                    Operation(
                        Type = "ScanBarcode",
                        TimeoutMs = timeoutMs
                    )
                )
            )

            val innerJsonString = Json {
                encodeDefaults = true
            }.encodeToString(innerJson)

            val base64Payload = Base64.getEncoder()
                .encodeToString(innerJsonString.toByteArray())

            // 2. Wrap in SaleToPOIRequest
            val request = SaleToPOIRequest(
                MessageHeader = MessageHeader(
                    MessageCategory = "Admin",
                    ServiceID = UUID.randomUUID().toString().take(10),
                    SaleID = saleId,
                    POIID = settings.poiId
                ),
                AdminRequest = AdminRequest(
                    ServiceIdentification = base64Payload
                )
            )

            // 3. Send request + handle response
            sendSecureRequest(request, settings).map { response ->

                val adminResponse = response.AdminResponse
                    ?: throw Exception("Missing AdminResponse")

                val result = adminResponse.Response.Result
                    ?: throw Exception("Missing AdminResponse.Response.Result")

                when (result) {
                    "Success" -> {
                        val encoded = adminResponse.Response.AdditionalResponse
                            ?: throw Exception("Missing AdditionalResponse on successful barcode scan")

                        val decodedJson = runCatching {
                            // Primary: Base64
                            String(Base64.getDecoder().decode(encoded))
                        }.getOrElse {
                            // Fallback: URL-encoded (Adyen may return this)
                            java.net.URLDecoder.decode(encoded, "UTF-8")
                        }

                        val json = Json { ignoreUnknownKeys = true }

                        val normalizedJson = decodedJson
                            .removePrefix("additionalData=")
                            .trim()

                        val barcodeResponse = try {
                            json.decodeFromString<BarcodeResponse>(normalizedJson)
                        } catch (e: Exception) {
                            throw Exception(
                                "Failed to parse barcode payload.\nDecoded payload:\n$normalizedJson"
                            )
                        }

                        BarcodeScanResult(
                            data = barcodeResponse.Barcode.Data,
                            symbology = barcodeResponse.Barcode.Symbology
                        )
                    }

                    "Failure" -> {
                        val encoded = adminResponse.Response.AdditionalResponse
                        val decoded = runCatching {
                            String(Base64.getDecoder().decode(encoded))
                        }.getOrElse {
                            Uri.decode(encoded)
                        }

                        throw Exception(
                            buildString {
                                append("Scan failed")
                                adminResponse.Response.ErrorCondition?.let { append(" ($it)") }
                                decoded?.let { append(": $it") }
                            }
                        )
                    }

                    else -> {
                        throw Exception("Unexpected AdminResponse.Result: $result")
                    }
                }
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
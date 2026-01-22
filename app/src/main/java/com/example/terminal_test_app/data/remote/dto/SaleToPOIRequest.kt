package com.example.terminal_test_app.data.remote.dto

import kotlinx.serialization.Serializable

// --- REQUESTS ---
data class SaleToPOIRequest(
    val MessageHeader: MessageHeader,
    val PaymentRequest: PaymentRequest? = null,
    val AdminRequest: AdminRequest? = null
)

data class MessageHeader(
    val ProtocolVersion: String = "3.0",
    val MessageClass: String = "Service",
    val MessageCategory: String, // Removed default to force explicit setting
    val MessageType: String = "Request",
    val ServiceID: String,
    val SaleID: String,
    val POIID: String
)

data class PaymentRequest(
    val SaleData: SaleData,
    val PaymentTransaction: PaymentTransaction
)

data class SaleData(val SaleTransactionID: SaleTransactionID)
data class SaleTransactionID(val TransactionID: String, val TimeStamp: String)

data class PaymentTransaction(
    val AmountsReq: AmountsReq // Use Req for Request
)

data class AmountsReq(
    val Currency: String,
    val RequestedAmount: Double
)

data class AdminRequest(
    val ServiceIdentification: String
)

// --- RESPONSES ---
data class SaleToPOIResponse(
    val MessageHeader: MessageHeaderResponse,
    val AdminResponse: AdminResponse? = null,
    val PaymentResponse: PaymentResponse? = null
)

data class PaymentResponse(
    val Response: Response
)

data class AdminResponse(
    val Response: Response,
    val AdditionalResponse: String?
)

data class Response(
    val Result: String, // "Success" or "Failure"
    val ErrorCondition: String? = null,
    val AdditionalResponse: String? = null
)

data class MessageHeaderResponse(
    val ProtocolVersion: String,
    val MessageClass: String,
    val MessageCategory: String,
    val MessageType: String,
    val ServiceID: String,
    val SaleID: String,
    val POIID: String
)

// --- SCANNER INNER JSON ---
@Serializable
data class ScanSessionJson(
    val Session: Session,
    val Operation: List<Operation>
)

@Serializable
data class Session(val Id: String, val Type: String = "Once")

@Serializable
data class Operation(val Type: String = "ScanBarcode", val TimeoutMs: Int)
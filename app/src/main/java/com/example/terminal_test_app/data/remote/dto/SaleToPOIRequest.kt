package com.example.terminal_test_app.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// --- WRAPPER DTOs ---

@Serializable
data class SaleToPOIRequest(
    @SerialName("MessageHeader") val MessageHeader: MessageHeader,
    @SerialName("PaymentRequest") val PaymentRequest: PaymentRequest? = null,
    @SerialName("AdminRequest") val AdminRequest: AdminRequest? = null
)

@Serializable
data class SaleToPOIResponse(
    @SerialName("MessageHeader") val MessageHeader: MessageHeaderResponse,
    @SerialName("AdminResponse") val AdminResponse: AdminResponse? = null,
    @SerialName("PaymentResponse") val PaymentResponse: PaymentResponse? = null
)

// --- HEADER DTOs ---

@Serializable
data class MessageHeader(
    @SerialName("ProtocolVersion") val ProtocolVersion: String = "3.0",
    @SerialName("MessageClass") val MessageClass: String = "Service",
    @SerialName("MessageCategory") val MessageCategory: String,
    @SerialName("MessageType") val MessageType: String = "Request",
    @SerialName("ServiceID") val ServiceID: String,
    @SerialName("SaleID") val SaleID: String,
    @SerialName("POIID") val POIID: String
)

@Serializable
data class MessageHeaderResponse(
    @SerialName("ProtocolVersion") val ProtocolVersion: String,
    @SerialName("MessageClass") val MessageClass: String,
    @SerialName("MessageCategory") val MessageCategory: String,
    @SerialName("MessageType") val MessageType: String,
    @SerialName("ServiceID") val ServiceID: String,
    @SerialName("SaleID") val SaleID: String,
    @SerialName("POIID") val POIID: String
)

// --- PAYMENT DTOs ---

@Serializable
data class PaymentRequest(
    @SerialName("SaleData") val SaleData: SaleData,
    @SerialName("PaymentTransaction") val PaymentTransaction: PaymentTransaction
)

@Serializable
data class PaymentResponse(
    @SerialName("Response") val Response: Response,
    @SerialName("SaleData") val SaleData: SaleData? = null
)

@Serializable
data class SaleData(
    @SerialName("SaleTransactionID") val SaleTransactionID: SaleTransactionID
)

@Serializable
data class SaleTransactionID(
    @SerialName("TransactionID") val TransactionID: String,
    @SerialName("TimeStamp") val TimeStamp: String
)

@Serializable
data class PaymentTransaction(
    @SerialName("AmountsReq") val AmountsReq: AmountsReq
)

@Serializable
data class AmountsReq(
    @SerialName("Currency") val Currency: String,
    @SerialName("RequestedAmount") val RequestedAmount: Double
)

// --- ADMIN / SCANNER DTOs ---

@Serializable
data class AdminRequest(
    @SerialName("ServiceIdentification") val ServiceIdentification: String
)

@Serializable
data class AdminResponse(
    @SerialName("Response") val Response: Response,
    @SerialName("AdditionalResponse") val AdditionalResponse: String? = null
)

@Serializable
data class Response(
    @SerialName("Result") val Result: String, // "Success", "Failure", "Partial"
    @SerialName("ErrorCondition") val ErrorCondition: String? = null,
    @SerialName("AdditionalResponse") val AdditionalResponse: String? = null
)

// --- SCANNER INNER JSON (The Payload inside the Base64 String) ---

@Serializable
data class ScanSessionJson(
    @SerialName("Session") val Session: Session,
    @SerialName("Operation") val Operation: List<Operation>
)

@Serializable
data class Session(
    @SerialName("Id") val Id: String,
    @SerialName("Type") val Type: String = "Once" // "Begin", "End", "Once"
)

@Serializable
data class Operation(
    @SerialName("Type") val Type: String = "ScanBarcode",
    @SerialName("TimeoutMs") val TimeoutMs: Int
)
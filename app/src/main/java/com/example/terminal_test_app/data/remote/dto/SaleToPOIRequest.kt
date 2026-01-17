package com.example.terminal_test_app.data.remote.dto
import kotlinx.serialization.Serializable

data class SaleToPOIRequest(
    val MessageHeader: MessageHeader,
    val AdminRequest: AdminRequest? = null  // Only for Admin calls
)

data class MessageHeader(
    val ProtocolVersion: String = "3.0",
    val MessageClass: String = "Service",
    val MessageCategory: String = "Admin",
    val MessageType: String = "Request",
    val ServiceID: String,
    val SaleID: String,
    val POIID: String  // e.g., "S1EL-123456789"
)

data class AdminRequest(
    val ServiceIdentification: String  // Base64-encoded inner JSON
)

// Inner JSON (encoded to Base64) - not sent directly
@Serializable
data class ScanSessionJson(
    val Session: Session,
    val Operation: List<Operation>
)

@Serializable
data class Session(
    val Id: String,  // e.g., "scan-uuid-123"
    val Type: String = "Once"
)

@Serializable
data class Operation(
    val Type: String = "ScanBarcode",
    val TimeoutMs: Int
)

// Response DTO
data class SaleToPOIResponse(
    val MessageHeader: MessageHeaderResponse,
    val AdminResponse: AdminResponse?
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

data class AdminResponse(
    val Response: Response,
    val AdditionalResponse: String?  // Base64-encoded result or error
)

data class Response(
    val Result: String,  // "Success" or "Failure"
    val AdditionalResponse: String? = null,  // Sometimes here too
    val ErrorCondition: String? = null
)
package com.example.terminal_test_app.data.remote.api

import com.example.terminal_test_app.data.remote.dto.SaleToPOIRequest
import com.example.terminal_test_app.data.remote.dto.SaleToPOIResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TerminalApi {
    @POST("/nexo")
    suspend fun sendAdminRequest(@Body request: SaleToPOIRequest): SaleToPOIResponse
}
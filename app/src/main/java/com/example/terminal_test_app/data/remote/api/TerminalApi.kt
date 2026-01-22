package com.example.terminal_test_app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

interface TerminalApi {
    @POST("/nexo")
    // Both input and output must be String to handle the encrypted Nexo JSON blob
    suspend fun sendAdminRequest(@Body request: String): String
}
package com.example.terminal_test_app.platform.device

import android.annotation.SuppressLint
import android.os.Build

object DeviceInfo {

    @SuppressLint("HardwareIds")
    fun getPoiId(): String? {
        return try {
            val serial = Build.SERIAL.takeIf { it.isNotBlank() && it != Build.UNKNOWN }
                ?: return null
            val model = Build.MODEL.takeIf { it.isNotBlank() }
                ?: return null
            "$model-$serial"
        } catch (e: SecurityException) {
            null // Permission not granted
        }
    }
}
package com.example.terminal_test_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class TerminalTestApp: Application() {
    override fun onCreate() {
        super.onCreate()

    }
}
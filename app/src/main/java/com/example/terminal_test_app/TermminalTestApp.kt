package com.example.terminal_test_app

import android.app.Application
import com.example.terminal_test_app.di.AppContainer

class TerminalTestApp: Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(applicationContext)
    }
}
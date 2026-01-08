// presentation/scan/ScanViewModelFactory.kt
package com.example.terminal_test_app.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.terminal_test_app.domain.usecase.ScanCodeUseCase
import com.example.terminal_test_app.platform.scan.AndroidCodeScanner

class ScanViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val scanner = AndroidCodeScanner()
        val useCase = ScanCodeUseCase(scanner)
        return ScanViewModel(useCase) as T
    }
}

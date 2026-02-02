package com.example.terminal_test_app.presentation.scan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.terminal_test_app.domain.model.ScanMethod
import com.example.terminal_test_app.domain.model.ScanResult
import com.example.terminal_test_app.platform.camera.CameraPreview
import com.example.terminal_test_app.platform.permission.CameraPermission


@Composable
fun ScanScreen(
    //viewModel: ScanViewModel = viewModel(factory = ScanViewModelFactory())
            viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()




    Column(modifier = Modifier.fillMaxSize()) {

        CameraBarCodeToggle(
            selected = uiState.scanType,
            onSelected = viewModel::setScanType,
            enabled = !uiState.isScanning
        )
        BackHandler(enabled = uiState.isScanning) {
            viewModel.cancelScan()
        }

        Spacer(Modifier.height(12.dp))

        uiState.debugMessage?.let { debug ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = debug,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        when {
            uiState.isScanning && uiState.scanType == ScanMethod.SCAN_QR_CODE -> {
                Box(modifier = Modifier.fillMaxSize()) {

                    CameraPermission {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onQrScanned = viewModel::onQrScanned
                        )
                    }

                    Button(
                        onClick = viewModel::cancelScan,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }


            uiState.isScanning && uiState.scanType == ScanMethod.SCAN_BAR_CODE -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Waiting for terminal barcode scan...")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::cancelScan) { Text("Cancel") }
                    }
                }
            }

            uiState.result is ScanResult.Cancelled -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Scan cancelled or failed")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::reset) {
                            Text("Try again")
                        }
                    }
                }
            }




            uiState.result is ScanResult.QrCode -> {
                QrResultView(
                    value = (uiState.result as ScanResult.QrCode).rawValue,
                    onScanAgain = viewModel::reset
                )
            }


            uiState.result is ScanResult.BarCode -> {
                QrResultView(
                    value = (uiState.result as ScanResult.BarCode).rawValue,
                    onScanAgain = viewModel::reset
                )
            }

            else -> {
                IdleScanView(
                    scanType = uiState.scanType,
                    onStartScan = viewModel::startScan
                )
            }
        }

    }
}



@Composable
fun CameraBarCodeToggle(
    selected: ScanMethod,
    onSelected: (ScanMethod) -> Unit,
    enabled: Boolean
) {
    val animatedOffset by animateDpAsState(
        targetValue = if (selected == ScanMethod.SCAN_QR_CODE) 0.dp else 125.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pillOffset"
    )

    Box(
        modifier = Modifier
            .size(width = 250.dp, height = 36.dp)
            .graphicsLayer {
                shape = RoundedCornerShape(18.dp)
                clip = true
            }
            .background(Color.LightGray.copy(alpha = 0.2f))
    ) {

        // Sliding pill
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .size(width = 125.dp, height = 36.dp)
                .padding(2.dp)
                .graphicsLayer {
                    shape = RoundedCornerShape(16.dp)
                    clip = true
                }
                .background(Color.White)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            ToggleLabel(
                text = "Scan QR Code",
                selected = selected == ScanMethod.SCAN_QR_CODE
            ) {
                onSelected(ScanMethod.SCAN_QR_CODE)
            }

            ToggleLabel(
                text = "Scan Bar Code",
                selected = selected == ScanMethod.SCAN_BAR_CODE
            ) {
                onSelected(ScanMethod.SCAN_BAR_CODE)
            }
        }
    }
}


@Composable
private fun RowScope.ToggleLabel(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = animateColorAsState(
                targetValue = if (selected) Color.Black else Color.Gray,
                animationSpec = tween(160),
                label = "textColor"
            ).value
        )
    }
}

@Composable
fun QrResultView(
    value: String,
    onScanAgain: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("QR Code scanned", fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onScanAgain) {
                Text("Scan again")
            }
        }
    }
}

@Composable
fun IdleScanView(
    scanType: ScanMethod,
    onStartScan: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (scanType == ScanMethod.SCAN_QR_CODE)
                    "Ready to scan a QR code"
                else
                    "Ready to scan a barcode"
            )

            Spacer(Modifier.height(16.dp))

            Button(onClick = onStartScan) {
                Text("Start scan")
            }
        }
    }
}

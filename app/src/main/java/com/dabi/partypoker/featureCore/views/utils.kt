package com.dabi.partypoker.featureCore.views

import android.util.Log
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowHeightSizeClass


@Composable
fun CalculatePlayerBoxSize(
    playerBoxSize: (DpSize) -> Unit,
    fontSize: (TextUnit) -> Unit
) {
    val configuration = LocalConfiguration.current
    LaunchedEffect(Unit) {
        when(configuration.screenWidthDp){
            in 0..750 -> {
                playerBoxSize(DpSize(100.dp, 50.dp))
                fontSize(10.sp)
            }
            in 751..1100 -> {
                playerBoxSize(DpSize(140.dp, 70.dp))
                fontSize(13.sp)
            }
            in 1101..1550 -> {
                playerBoxSize(DpSize(180.dp, 90.dp))
                fontSize(18.sp)
            }
            else -> {
                playerBoxSize(DpSize(220.dp, 110.dp))
                fontSize(23.sp)
            }
        }
    }
}
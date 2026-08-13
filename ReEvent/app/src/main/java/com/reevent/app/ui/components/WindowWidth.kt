package com.reevent.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun currentWindowWidthDp(): Dp {
    val widthPx = LocalWindowInfo.current.containerSize.width
    return if (widthPx > 0) {
        with(LocalDensity.current) { widthPx.toDp() }
    } else {
        0.dp
    }
}

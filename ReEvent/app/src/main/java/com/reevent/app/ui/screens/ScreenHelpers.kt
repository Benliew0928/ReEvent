package com.reevent.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.reevent.app.ui.components.currentWindowWidthDp

enum class TwoPaneLayout { STACKED, SIDE_BY_SIDE }

internal fun twoPaneLayoutForWidth(widthDp: Float): TwoPaneLayout =
    if (widthDp < 420f) TwoPaneLayout.STACKED else TwoPaneLayout.SIDE_BY_SIDE

@Composable
fun AdaptiveTwoPane(
    modifier: Modifier = Modifier,
    stackedAlignment: Alignment.Horizontal = Alignment.Start,
    layout: TwoPaneLayout = twoPaneLayoutForWidth(currentWindowWidthDp().value),
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
) {
    when (layout) {
        TwoPaneLayout.STACKED -> {
            Column(
                modifier = modifier.testTag("adaptive-stacked"),
                horizontalAlignment = stackedAlignment,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                first()
                second()
            }
        }

        TwoPaneLayout.SIDE_BY_SIDE -> {
            Row(
                modifier = modifier.testTag("adaptive-side-by-side"),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) { first() }
                Box(modifier = Modifier.weight(1f)) { second() }
            }
        }
    }
}

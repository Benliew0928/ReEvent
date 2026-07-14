package com.reevent.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ── Plan-aligned shape tokens ──────────────────────────────────────────
val CardRadius       = 8.dp
val ButtonRadius     = 8.dp
val ImageRadius      = 8.dp
val ChipRadius       = 18.dp
val BottomSheetRadius = 24.dp

val ReEventShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(CardRadius),
    medium     = RoundedCornerShape(14.dp),
    large      = RoundedCornerShape(ChipRadius),
    extraLarge = RoundedCornerShape(BottomSheetRadius)
)

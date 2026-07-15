package com.reevent.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Plan-aligned color tokens ──────────────────────────────────────────
// Background & surface
val ReEventBackground  = Color(0xFFF8F2E8)
val ReEventSurface     = Color(0xFFFFFCF6)
val ReEventSurfaceAlt  = Color(0xFFF1E7DA)

// Text
val ReEventInk            = Color(0xFF14201B)
val ReEventTextSecondary  = Color(0xFF56635D)
val ReEventTextMuted      = Color(0xFF7A867F)
val ReEventLine           = Color(0xFFE3D8CA)

// Primary green
val ReEventGreen     = Color(0xFF08785F)
val ReEventGreenRich = Color(0xFF009B72)
val ReEventGreenDeep = Color(0xFF003E33)
val ReEventMint      = Color(0xFFDDF4E9)
val ReEventMintSoft  = Color(0xFFE3F3DF)

// Blue
val ReEventBlue     = Color(0xFF2F6FED)
val ReEventBlueSoft = Color(0xFFE8F0FF)

// Amber / warm
val ReEventAmber     = Color(0xFFF6A83A)
val ReEventAmberSoft = Color(0xFFFFF1D2)

// Coral / error
val ReEventCoral     = Color(0xFFE56B6F)
val ReEventCoralSoft = Color(0xFFFFE6E8)

// ── Legacy aliases (used throughout the existing codebase) ──────────
// These map old names → plan-aligned values so every existing import
// still compiles, but the actual color is now the plan value.
val ReEventCanvas = ReEventBackground
val ReEventPaper  = ReEventSurface
val ReEventMuted  = ReEventTextSecondary
val ReEventWarm   = ReEventAmber

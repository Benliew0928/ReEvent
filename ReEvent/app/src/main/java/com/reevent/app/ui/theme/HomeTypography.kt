package com.reevent.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.reevent.app.R

val HomeEditorialFont = FontFamily(
    Font(R.font.cormorant_garamond, FontWeight.Normal),
)

val HomeBodyFont = FontFamily(
    Font(R.font.source_sans_3, FontWeight.Normal),
)

val HomeGreetingStyle = TextStyle(
    fontFamily = HomeEditorialFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 30.sp,
    lineHeight = 34.sp,
)

val HomeHeroTitleStyle = TextStyle(
    fontFamily = HomeEditorialFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 44.sp,
    lineHeight = 43.sp,
)

val HomeSectionTitleStyle = TextStyle(
    fontFamily = HomeEditorialFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 27.sp,
    lineHeight = 32.sp,
)

val HomeCardTitleStyle = TextStyle(
    fontFamily = HomeEditorialFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 21.sp,
    lineHeight = 25.sp,
)

val HomeBodyStyle = TextStyle(
    fontFamily = HomeBodyFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
)

val HomeSupportingTextStyle = TextStyle(
    fontFamily = HomeBodyFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

val HomeLabelStyle = TextStyle(
    fontFamily = HomeBodyFont,
    fontWeight = FontWeight.Bold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.7.sp,
)

package com.example.azm_gymtracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.Font
import com.example.azm_gymtracker.R

// Arabic Thmanyah Sans typography scale from Figma (Node #29:1840)
val ThmanyahSansFamily = FontFamily(
    Font(R.font.thmanyah_regular, FontWeight.Normal),
    Font(R.font.thmanyah_medium, FontWeight.Medium),
    Font(R.font.thmanyah_bold, FontWeight.Bold),
    Font(R.font.thmanyah_black, FontWeight.Black)
)

val Typography = Typography(
    // 48 / أرقام العرض (Black 900)
    displayLarge = TextStyle(
        fontFamily = ThmanyahSansFamily,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        lineHeight = (48 * 1.4).sp,
        letterSpacing = 0.sp
    ),
    // 28 / عنوان رئيسي (Bold 700)
    headlineLarge = TextStyle(
        fontFamily = ThmanyahSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = (28 * 1.4).sp,
        letterSpacing = 0.sp
    ),
    // 24 / أرقام وإحصائيات (Bold 700)
    headlineMedium = TextStyle(
        fontFamily = ThmanyahSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = (24 * 1.4).sp,
        letterSpacing = 0.sp
    ),
    // 20 / عنوان فرعي (Bold 700)
    titleLarge = TextStyle(
        fontFamily = ThmanyahSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = (20 * 1.4).sp,
        letterSpacing = 0.sp
    ),
    // 16 / نص أساسي وعناوين البطاقات (Bold 700)
    titleMedium = TextStyle(
        fontFamily = ThmanyahSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = (16 * 1.4).sp,
        letterSpacing = 0.sp
    ),
    // 16 / نص أساسي (Regular 400)
    bodyLarge = TextStyle(
        fontFamily = ThmanyahSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = (16 * 1.4).sp,
        letterSpacing = 0.sp
    ),
    // 14 / تسمية وإدخال (Medium 500)
    bodyMedium = TextStyle(
        fontFamily = ThmanyahSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = (14 * 1.4).sp,
        letterSpacing = 0.sp
    ),
    // 12 / توضيح وشارات (Regular 400)
    bodySmall = TextStyle(
        fontFamily = ThmanyahSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = (12 * 1.4).sp,
        letterSpacing = 0.sp
    ),
    // 12 / شارات وتسميات مصغرة (Bold 700)
    labelSmall = TextStyle(
        fontFamily = ThmanyahSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = (12 * 1.4).sp,
        letterSpacing = 0.sp
    )
)
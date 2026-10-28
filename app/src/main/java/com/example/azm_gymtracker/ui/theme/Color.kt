package com.example.azm_gymtracker.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand Accent Colors (From Figma Node #29:1639 & #87:125)
val AzmNeonGreen = Color(0xFF8CFF68)
val AzmEmeraldGreen = Color(0xFF3FE779)
val AzmDeepGreen = Color(0xFF0C8F48)
val AzmDeepForestGreen = AzmDeepGreen

// Dark Theme Surfaces (Gym Default Mode)
val AzmDarkBackground = Color(0xFF06110D)
val AzmDarkSurface = Color(0xFF0B1B14)
val AzmDarkSurfaceSelected = Color(0xFF153E27)
val AzmDarkSurfaceVariant = Color(0xFF10271C)
val AzmBorderSubtle = Color(0xFF284237)
val AzmMutedGreen = AzmBorderSubtle

// Dark Theme Text & Content
val AzmDarkOnSurface = Color(0xFFF4FFF5)
val AzmDarkOnSurfaceMuted = Color(0xFF9AAFA0)
val AzmTextMuted = AzmDarkOnSurfaceMuted

// Light Theme Surfaces (Figma Node #87:1010)
val AzmLightBackground = Color(0xFFF1F8F3)
val AzmLightSurface = Color(0xFFFFFFFF)
val AzmLightSurfaceSelected = Color(0xFFE1F3E7)
val AzmLightSurfaceVariant = Color(0xFFEAF4ED)
val AzmLightBorder = Color(0xFFCBDCD0)
val AzmLightOnSurface = Color(0xFF0B2115)
val AzmLightOnSurfaceMuted = Color(0xFF4B6252)
val AzmLightPrimary = Color(0xFF08743D)

// Multi-Participant Identity Palette (Figma Node #29:1822)
val MemberColorGreen = Color(0xFF3FE779)  // أحمد (أ)
val MemberColorGold = Color(0xFFFFD177)   // محمد (م) & الأرقام القياسية PR
val MemberColorIndigo = Color(0xFF8E9CFF) // سلمان (س)
val MemberColorCoral = Color(0xFFE9A87C)  // خالد (خ)

// Functional & Feedback
val AzmError = Color(0xFFFF7E7E)
val AzmLightError = Color(0xFFB42332)
val AzmWarning = Color(0xFFFFD177)
val AzmRecordGold = Color(0xFFFFD177)
val AzmSuccess = Color(0xFF3FE779)

// Primary CTA Button Gradient (Figma: 120°/127° #8CFF68 -> #42E879 -> #0C8F48)
val AzmPrimaryGradient = Brush.linearGradient(
    colors = listOf(
        AzmNeonGreen,
        AzmEmeraldGreen,
        AzmDeepGreen
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

// Hero Card Gradient Dark (135° #145C33 -> #0B1B14)
val AzmHeroCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF145C33),
        Color(0xFF0B1B14)
    )
)

// Hero Card Gradient Light (Figma Node #87:1039: 135° #D6F9DF -> #FFFFFF)
val AzmLightHeroCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFD6F9DF),
        Color(0xFFFFFFFF)
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)
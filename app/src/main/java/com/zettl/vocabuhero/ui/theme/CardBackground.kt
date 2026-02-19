package com.zettl.vocabuhero.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Describes a practice card background preset. */
data class CardBackgroundPreset(
    val id: Int,
    val displayName: String,
    val containerColor: Color?,
    val gradientBrush: Brush? = null,
    val contentColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    /** Color used for preview swatch in settings (e.g. first gradient color or containerColor). */
    val previewColor: Color
) {
    val isGradient: Boolean get() = gradientBrush != null
}

object CardBackgrounds {
    /** 0 = Theme, 1 = Light, 2 = Dark, 3–7 = custom presets */
    const val MAX_ID = 7

    fun preset(
        id: Int,
        surfaceVariant: Color,
        primaryContainer: Color,
        onSurface: Color,
        onPrimaryContainer: Color,
        primary: Color,
        surface: Color,
        onSurfaceVariant: Color
    ): CardBackgroundPreset? {
        return when (id) {
            0 -> CardBackgroundPreset(
                id = 0,
                displayName = "Theme",
                containerColor = primaryContainer,
                contentColor = onPrimaryContainer,
                secondaryColor = onPrimaryContainer.copy(alpha = 0.9f),
                tertiaryColor = onPrimaryContainer.copy(alpha = 0.8f),
                previewColor = primaryContainer
            )
            1 -> CardBackgroundPreset(
                id = 1,
                displayName = "Light",
                containerColor = surface,
                contentColor = onSurface,
                secondaryColor = primary,
                tertiaryColor = onSurfaceVariant,
                previewColor = surface
            )
            2 -> CardBackgroundPreset(
                id = 2,
                displayName = "Dark",
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White,
                secondaryColor = Color.White.copy(alpha = 0.9f),
                tertiaryColor = Color.White.copy(alpha = 0.8f),
                previewColor = Color(0xFF1C1C1E)
            )
            3 -> CardBackgroundPreset(
                id = 3,
                displayName = "Ocean",
                containerColor = null,
                gradientBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D7377),
                        Color(0xFF0a4d50)
                    )
                ),
                contentColor = Color.White,
                secondaryColor = Color(0xFFA8F0E8),
                tertiaryColor = Color.White.copy(alpha = 0.85f),
                previewColor = Color(0xFF0D7377)
            )
            4 -> CardBackgroundPreset(
                id = 4,
                displayName = "Sunset",
                containerColor = null,
                gradientBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF6B6B),
                        Color(0xFFC44569)
                    )
                ),
                contentColor = Color.White,
                secondaryColor = Color(0xFFFFE5B4),
                tertiaryColor = Color.White.copy(alpha = 0.9f),
                previewColor = Color(0xFFFF6B6B)
            )
            5 -> CardBackgroundPreset(
                id = 5,
                displayName = "Aurora",
                containerColor = null,
                gradientBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                ),
                contentColor = Color.White,
                secondaryColor = Color(0xFFD4BFFF),
                tertiaryColor = Color.White.copy(alpha = 0.85f),
                previewColor = Color(0xFF667eea)
            )
            6 -> CardBackgroundPreset(
                id = 6,
                displayName = "Mint",
                containerColor = Color(0xFF7BC4A0),
                contentColor = Color(0xFF1A2F1E),
                secondaryColor = Color(0xFFE8F5E9),
                tertiaryColor = Color(0xFF2D3D30),
                previewColor = Color(0xFF7BC4A0)
            )
            7 -> CardBackgroundPreset(
                id = 7,
                displayName = "Slate",
                containerColor = Color(0xFF334155),
                contentColor = Color.White,
                secondaryColor = Color(0xFFCBD5E1),
                tertiaryColor = Color.White.copy(alpha = 0.8f),
                previewColor = Color(0xFF334155)
            )
            else -> null
        }
    }

    fun allPresets(
        surfaceVariant: Color,
        primaryContainer: Color,
        onSurface: Color,
        onPrimaryContainer: Color,
        primary: Color,
        surface: Color,
        onSurfaceVariant: Color
    ): List<CardBackgroundPreset> {
        return (0..MAX_ID).mapNotNull { id ->
            preset(id, surfaceVariant, primaryContainer, onSurface, onPrimaryContainer, primary, surface, onSurfaceVariant)
        }
    }
}

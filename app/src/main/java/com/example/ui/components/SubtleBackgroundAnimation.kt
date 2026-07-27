package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun SubtleBackgroundAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")

    val float1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "f1"
    )

    val float2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "f2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val c1X = width * (0.25f + 0.35f * float1)
        val c1Y = height * (0.2f + 0.25f * float2)
        val c1Radius = width * 0.5f

        val c2X = width * (0.75f - 0.35f * float2)
        val c2Y = height * (0.7f - 0.25f * float1)
        val c2Radius = width * 0.55f

        // Soft Warm Gold/Amber Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFF59E0B).copy(alpha = 0.08f),
                    Color(0x00FAF6EE)
                ),
                center = Offset(c1X, c1Y),
                radius = c1Radius
            ),
            center = Offset(c1X, c1Y),
            radius = c1Radius
        )

        // Soft Emerald/Blue Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3B82F6).copy(alpha = 0.06f),
                    Color(0x00FAF6EE)
                ),
                center = Offset(c2X, c2Y),
                radius = c2Radius
            ),
            center = Offset(c2X, c2Y),
            radius = c2Radius
        )
    }
}

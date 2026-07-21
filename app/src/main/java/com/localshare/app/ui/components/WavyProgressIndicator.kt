package com.localshare.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularWavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    strokeWidth: Float = 10f,
    waves: Int = 8 // Number of squiggles
) {
    val indicatorColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary else color
    
    val infiniteTransition = rememberInfiniteTransition(label = "wavy_transition")
    // This phase animation creates the rippling effect
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_animation"
    )
    // This rotation animation rotates the entire circle
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_animation"
    )

    Canvas(
        modifier = modifier.graphicsLayer { rotationZ = rotation }
    ) {
        val radius = (minOf(size.width, size.height) - strokeWidth) / 2f
        val amplitude = radius * 0.15f // Wave height relative to radius
        val baseRadius = radius - amplitude
        val center = Offset(size.width / 2f, size.height / 2f)
        
        val path = Path()
        var first = true

        // Draw points around the circle
        for (i in 0..360 step 2) {
            val angle = i * (PI.toFloat() / 180f)
            // The wave oscillates 'waves' times around the circle
            val waveOffset = amplitude * sin(waves * angle - phase * waves)
            val r = baseRadius + waveOffset
            
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)

            if (first) {
                path.moveTo(x, y)
                first = false
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        drawPath(
            path = path,
            color = indicatorColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

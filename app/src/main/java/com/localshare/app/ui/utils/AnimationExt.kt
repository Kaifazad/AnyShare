package com.localshare.app.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.LocalIndication

/**
 * Haptic feedback helper — provides light vibration on supported devices.
 */
object HapticHelper {
    var enabled = true
    fun performClick(context: Context) {
        if (!enabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: SecurityException) {
            // Vibrate permission not granted — skip haptic feedback
        }
    }

    /**
     * Double-tap vibration pattern for transfer completion.
     * Two quick pulses: 30ms on, 60ms off, 30ms on.
     */
    fun performTransferComplete(context: Context) {
        if (!enabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 30, 60, 30)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } catch (_: SecurityException) {
            // Vibrate permission not granted — skip haptic feedback
        }
    }
}

/**
 * A custom modifier that adds the modern Android 17 (Material 3 Expressive) bounce physics
 * when a component is pressed. It scales the component down smoothly with a spring animation
 * and provides haptic feedback.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    showRipple: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = if (showRipple) LocalIndication.current else null,
            onClick = {
                HapticHelper.performClick(context)
                onClick()
            }
        )
}

/**
 * An alternative modifier if you just want the bounce scaling effect but are handling
 * the clickable/interactionSource externally (e.g., inside a Material Button).
 */
@Composable
fun Modifier.bounceScale(
    interactionSource: MutableInteractionSource,
    scaleDown: Float = 0.95f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceScale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

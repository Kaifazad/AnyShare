package com.localshare.app.ui.utils

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
import androidx.compose.foundation.LocalIndication

/**
 * A custom modifier that adds the modern Android 17 (Material 3 Expressive) bounce physics
 * when a component is pressed. It scales the component down smoothly with a spring animation.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    showRipple: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
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
            onClick = onClick
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

package com.tezov.medium.adr.transition_animation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

class AnimationContent(
    val id: String,
    val animationConfig: Animations.Config = Animations.Config(),
    internal val content: @Composable BoxScope.() -> Unit
) {
    var modifierAnimation: Animations.ModifierAnimation
        = Animations.None()
    @Composable
    fun compose() {
        Box(modifier = modifierAnimation) {
            content()
        }
    }
}
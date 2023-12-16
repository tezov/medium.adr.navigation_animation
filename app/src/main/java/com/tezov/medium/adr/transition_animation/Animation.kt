package com.tezov.medium.adr.transition_animation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.tezov.medium.adr.transition_animation.AnimationProgress.Companion.updateAnimationProgress
import com.tezov.medium.adr.transition_animation.NavigationAnimation.Config.*
import com.tezov.medium.adr.transition_animation.NavigationAnimation.Fade.fade
import com.tezov.medium.adr.transition_animation.NavigationAnimation.Slide.Horizontal.slideHorizontal
import com.tezov.medium.adr.transition_animation.NavigationAnimation.Slide.Vertical.slideVertical

class Content(
    val id: String,
    val animationConfig: NavigationAnimation.Config,
    internal val content: @Composable BoxScope.() -> Unit
) {
    lateinit var modifierAnimation: NavigationAnimation.ModifierAnimation

    @Composable
    fun compose() {
        Box(
            modifier = modifierAnimation
        ) {
            content()
        }
    }

}

@Composable
fun animate(
    screens:List<Content>,
    showId:String,
    isNavigatingBack:Boolean
) {
    remember(key1 = screens) {
        ScreenTransitionAnimation(screens = screens)
    }.also {
        it.compose(showId = showId, isNavigatingBack = isNavigatingBack)
    }
}

private class ScreenTransitionAnimation(
    private val screens:List<Content>
){
    var isIdle = true
    var isNavigatingBack = false

    lateinit var currentScreenId:String
    lateinit var showId:String

    val lastEntry =  when {
        !isIdle && isNavigatingBack -> {
            screens.first { it.id == showId }
        }
        else ->  screens.first { it.id == currentScreenId }
    }
    val priorEntry = when {
        !isIdle && isNavigatingBack -> {
            screens.first { it.id == currentScreenId }
        }
        else ->  screens.first { it.id == showId }
    }

    @Composable
    fun compose(
        showId:String,
        isNavigatingBack:Boolean
    ) {
        this.isIdle = currentScreenId == showId
        this.isNavigatingBack = isNavigatingBack
        this.showId = showId
        this.currentScreenId = remember { showId }

        updateTransition()

        val stateHolder = rememberSaveableStateHolder()
        screens.forEach {
            key(it.id) {
                stateHolder.SaveableStateProvider(it.id) {
                    it.content
                }
            }
        }
    }

    @Composable
    private fun updateTransition() {
        val coroutineScope = rememberCoroutineScope()
        val transition = updateAnimationProgress()

        val animationConfigResolved = if (isNavigatingBack) {
            priorEntry.animationConfig
        } else {
            lastEntry.animationConfig
        }
        priorEntry.updateTransition(
            transition = transition,
            animationConfig = animationConfigResolved,
            directionContent = NavigationAnimation.Direction.Content.Exit,
            isNavigatingBack = isNavigatingBack,
        )
        lastEntry.updateTransition(
            transition = transition,
            animationConfig = animationConfigResolved,
            directionContent = NavigationAnimation.Direction.Content.Enter,
            isNavigatingBack = isNavigatingBack,
        )
        if (transition.isIdle) {
            transition.collect.once(coroutineScope) {
                currentScreenId = showId
            }
        }
        transition.start()
    }

    private fun Content.updateTransition(
        transition: AnimationProgress,
        animationConfig: NavigationAnimation.Config,
        directionContent: NavigationAnimation.Direction.Content,
        isNavigatingBack: Boolean,
    ) {
        val type = when (directionContent) {
            NavigationAnimation.Direction.Content.Enter -> when {
                isNavigatingBack -> animationConfig.enter.pop
                else -> animationConfig.enter.push
            }

            NavigationAnimation.Direction.Content.Exit -> when {
                isNavigatingBack -> animationConfig.exit.pop
                else -> animationConfig.exit.push
            }
        }
        when (type) {
            is Type.None -> {
                modifierAnimation = NavigationAnimation.None()
            }

            is Type.Fade -> {
                modifierAnimation = transition.fade(
                    config = type,
                    directionContent = directionContent,
                )
            }

            is Type.SlideHorizontal -> {
                modifierAnimation = transition.slideHorizontal(
                    config = type,
                    directionNav = when (isNavigatingBack) {
                        true -> NavigationAnimation.Direction.Nav.Pop
                        false -> NavigationAnimation.Direction.Nav.Push
                    },
                    directionContent = directionContent,
                )
            }

            is Type.SlideVertical -> {
                modifierAnimation = transition.slideVertical(
                    config = type,
                    directionNav = when (isNavigatingBack) {
                        true -> NavigationAnimation.Direction.Nav.Pop
                        false -> NavigationAnimation.Direction.Nav.Push
                    },
                    directionContent = directionContent,
                )
            }
        }
    }

}







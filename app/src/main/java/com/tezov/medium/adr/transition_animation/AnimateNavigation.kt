package com.tezov.medium.adr.transition_animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tezov.medium.adr.transition_animation.AnimationProgress.Companion.updateAnimationProgress
import com.tezov.medium.adr.transition_animation.Animations.Config.*
import com.tezov.medium.adr.transition_animation.Animations.Direction.Content.*
import com.tezov.medium.adr.transition_animation.Animations.Fade.fade
import com.tezov.medium.adr.transition_animation.Animations.Slide.Horizontal.slideHorizontal
import com.tezov.medium.adr.transition_animation.Animations.Slide.Vertical.slideVertical

@Composable
fun AnimateNavigation(
    screens:List<AnimationContent>,
    showId:String,
    isNavigatingBack:Boolean
) {
    remember {
        ScreenTransitionAnimation(screens = screens)
    }.also {
        it.compose(showId = showId, isNavigatingBack = isNavigatingBack)
    }
}

private class ScreenTransitionAnimation(
    private val screens:List<AnimationContent>
){
    var isNavigatingBack = false

    lateinit var currentScreenId:String
    lateinit var showId:String

    val lastEntry get() =  screens.first { it.id == showId }
    val priorEntry get() = screens.first { it.id == currentScreenId }

    val visibleStack get() = if (isNavigatingBack) {
        listOf(lastEntry, priorEntry)
    } else {
        listOf(priorEntry, lastEntry)
    }.distinctBy { it.id }

    @Composable
    fun compose(
        showId:String,
        isNavigatingBack:Boolean
    ) {
        if(!this::currentScreenId.isInitialized) {
            this.currentScreenId = showId
        }
        this.showId = showId
        this.isNavigatingBack = isNavigatingBack

        val visibleStack = this.visibleStack
        updateTransition(visibleStack = visibleStack)
        visibleStack.forEach { it.compose()}
    }

    @Composable
    private fun updateTransition(
        visibleStack: List<AnimationContent>
    ) {
        if(currentScreenId == showId) return

        val priorEntry:AnimationContent
        val lastEntry:AnimationContent
        if(isNavigatingBack){
            priorEntry = visibleStack[1]
            lastEntry = visibleStack[0]
        }
        else {
            priorEntry = visibleStack[0]
            lastEntry = visibleStack[1]
        }
        val animationConfigResolved = visibleStack[1].animationConfig

        val coroutineScope = rememberCoroutineScope()
        val transition = updateAnimationProgress()

        priorEntry.updateTransition(
            transition = transition,
            animationConfig = animationConfigResolved,
            directionContent = Exit
        )
        lastEntry.updateTransition(
            transition = transition,
            animationConfig = animationConfigResolved,
            directionContent = Enter
        )

        if (transition.isIdle) {
            transition.collect.once(coroutineScope) {
                priorEntry.modifierAnimation = Animations.None()
                lastEntry.modifierAnimation = Animations.None()
                currentScreenId = showId
            }
            transition.start()
        }
    }

    private fun AnimationContent.updateTransition(
        transition: AnimationProgress,
        animationConfig: Animations.Config,
        directionContent: Animations.Direction.Content
    ) {
        val type = when (directionContent) {
            Enter -> when {
                isNavigatingBack -> animationConfig.enter.pop
                else -> animationConfig.enter.push
            }

            Exit -> when {
                isNavigatingBack -> animationConfig.exit.pop
                else -> animationConfig.exit.push
            }
        }
        when (type) {
            is Type.None -> {
                modifierAnimation = Animations.None()
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
                        true -> Animations.Direction.Nav.Pop
                        false -> Animations.Direction.Nav.Push
                    },
                    directionContent = directionContent,
                )
            }

            is Type.SlideVertical -> {
                modifierAnimation = transition.slideVertical(
                    config = type,
                    directionNav = when (isNavigatingBack) {
                        true -> Animations.Direction.Nav.Pop
                        false -> Animations.Direction.Nav.Push
                    },
                    directionContent = directionContent,
                )
            }
        }
    }

}







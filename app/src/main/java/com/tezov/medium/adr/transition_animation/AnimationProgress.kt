package com.tezov.medium.adr.transition_animation

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.tezov.medium.adr.transition_animation.notifier.Notifier

class AnimationProgress private constructor() {

    private enum class Step { Start_Idle, Running, End_Idle }

    private lateinit var isStarted: MutableState<Boolean>
    private lateinit var transitionState: MutableTransitionState<Step>
    private lateinit var transition: Transition<Step>
    private val notifier = Notifier.Emitter<Unit>()
    val collect get() = notifier.createCollector

    val isIdle
        get() = !isStarted.value && (transitionState.currentState == Step.Start_Idle
                || transitionState.currentState == Step.End_Idle)

    fun start() {
        if(isStarted.value) return
        transitionState.targetState = Step.Start_Idle
        isStarted.value = true
    }

    @Composable
    private fun updateTransition() {
        isStarted = remember {
            mutableStateOf(false)
        }
        transitionState = remember {
            mutableStateOf(
                MutableTransitionState(Step.Start_Idle)
            )
        }.value
        with(transitionState) {
            if (isIdle) {
                when (currentState) {
                    Step.Start_Idle -> {
                        if(isStarted.value){
                            targetState = Step.Running
                        }
                    }
                    Step.Running -> {
                        targetState = Step.End_Idle
                    }
                    Step.End_Idle -> {
                        if(isStarted.value){
                            isStarted.value = false
                            notifier.tryEmit(Unit)
                        }
                    }
                }
            }
        }
        transition = updateTransition(transitionState = transitionState, label = "")
    }

    @Composable
    fun animateFloat(
        startValue: Float = START_VALUE,
        endValue: Float = END_VALUE,
        animationSpecToEnd: FiniteAnimationSpec<Float> = remember {
            spring()
        }
    ):State<Float> {
        val clampedValue = remember {
            mutableFloatStateOf(startValue)
        }
        val animatedFloat = transition.animateFloat(
            transitionSpec = {
                when (transitionState.targetState) {
                    Step.Start_Idle, Step.End_Idle -> snap(delayMillis = 0)
                    else -> animationSpecToEnd
                }
            },
            label = "",
        ) {
            when (it) {
                Step.Start_Idle -> startValue
                else -> endValue
            }
        }
        clampedValue.floatValue = when (transitionState.targetState) {
            Step.Running -> animatedFloat.value
            Step.Start_Idle -> startValue
            Step.End_Idle -> endValue
        }
        return clampedValue
    }

    companion object {

        private const val START_VALUE = 0.0f
        private const val END_VALUE = 1.0f

        @Composable
        fun updateAnimationProgress() = remember {
            AnimationProgress()
        }.also {
            it.updateTransition()
        }

    }

}
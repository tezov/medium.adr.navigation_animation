package com.tezov.medium.adr.transition_animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp

object Animations {

    class Config private constructor(default: Type) {

        companion object{

            operator fun invoke(default: Type = Type.Fade(), block: Config.() -> Unit = {}) =
                Config(default).also(block)
        }

        sealed class Type {
            object None : Type()

            class Fade(
                val duration_ms: Int = Animations.Fade.DURATION_ms
            ) : Type()

            class SlideHorizontal(
                val duration_ms: Int = Slide.Horizontal.DURATION_ms,
                val outDarkAlphaFactor: Float = Slide.Horizontal.OUT_DARK_ALPHA_FACTOR,
                val entrance: Slide.Horizontal.Entrance = Slide.Horizontal.Entrance.FromEnd,
                val effect: Slide.Effect = Slide.Effect.CoverPush,
            ) : Type()

            class SlideVertical(
                val duration_ms: Int = Slide.Vertical.DURATION_ms,
                val outDarkAlphaFactor: Float = Slide.Vertical.OUT_DARK_ALPHA_FACTOR,
                val entrance: Slide.Vertical.Entrance = Slide.Vertical.Entrance.FromBottom,
                val effect: Slide.Effect = Slide.Effect.CoverPush,
            ) : Type()
        }

        class Scope(default: Type) {

            var push: Type = default

            var pop: Type = default

        }

        internal var enter: Scope = Scope(default)
        internal var exit: Scope = Scope(default)

        fun enter(block: Scope.() -> Unit) {
            enter.block()
        }

        fun exit(block: Scope.() -> Unit) {
            exit.block()
        }
    }

    object Direction {
        enum class Nav { Push, Pop }
        enum class Content { Enter, Exit }
    }

    abstract class ModifierAnimation : Modifier {
        private val modifier = Modifier.composed {
            animate(Size(400f, 200f))
        }

        @Composable
        abstract fun Modifier.animate(boundaries:Size): Modifier

        final override fun all(predicate: (Modifier.Element) -> Boolean) = modifier.all(predicate)

        final override fun any(predicate: (Modifier.Element) -> Boolean) = modifier.any(predicate)

        final override fun <R> foldIn(initial: R, operation: (R, Modifier.Element) -> R): R =
            modifier.foldIn(initial, operation)

        final override fun <R> foldOut(initial: R, operation: (Modifier.Element, R) -> R): R =
            modifier.foldOut(initial, operation)
    }

    class None : ModifierAnimation() {

        @Composable
        override fun Modifier.animate(boundaries:Size) = this
    }

    object Fade {

        const val DURATION_ms = 250

        fun AnimationProgress.fade(
            config: Config.Type.Fade,
            directionContent: Direction.Content
        ): ModifierAnimation {
            return when (directionContent) {
                Direction.Content.Enter -> In(config, this)
                Direction.Content.Exit -> Out(config, this)
            }
        }

        class In(
            private val config: Config.Type.Fade,
            private val animationProgress: AnimationProgress,
        ) : ModifierAnimation() {

            @Composable
            override fun Modifier.animate(boundaries:Size): Modifier {
                val progress = animationProgress.animateFloat(
                    startValue = 0.0f,
                    endValue = 1.0f,
                    animationSpecToEnd = tween(
                        durationMillis = config.duration_ms,
                        easing = LinearEasing
                    )
                )
                return this
                    .alpha(progress.value)
            }
        }

        class Out(
            private val config: Config.Type.Fade,
            private val animationProgress: AnimationProgress,
        ) : ModifierAnimation() {

            @Composable
            override fun Modifier.animate(boundaries:Size): Modifier {
                val progress = animationProgress.animateFloat(
                    startValue = 1.0f,
                    endValue = 0.5f,
                    animationSpecToEnd = tween(
                        durationMillis = config.duration_ms,
                        easing = LinearEasing
                    )
                )
                return this
                    .alpha(progress.value)
            }
        }

    }

    object Slide {

        enum class Effect { CoverPush, Cover, Push }

        object Horizontal {

            internal const val DURATION_ms = 200
            internal const val OUT_DARK_ALPHA_FACTOR = 0.75f

            enum class Entrance { FromEnd, FromStart }

            internal fun AnimationProgress.slideHorizontal(
                config: Config.Type.SlideHorizontal,
                directionNav: Direction.Nav,
                directionContent: Direction.Content,
            ): ModifierAnimation {
                return when (directionContent) {
                    Direction.Content.Enter -> when (directionNav) {
                        Direction.Nav.Push -> In(config, this, Direction.Nav.Push)
                        Direction.Nav.Pop -> Out(config, this, Direction.Nav.Pop)
                    }
                    Direction.Content.Exit -> when (directionNav) {
                        Direction.Nav.Push -> Out(config, this, Direction.Nav.Push)
                        Direction.Nav.Pop -> In(config, this, Direction.Nav.Pop)
                    }
                }
            }

            internal class In(
                private val config: Config.Type.SlideHorizontal,
                private val animationProgress: AnimationProgress,
                directionNav: Direction.Nav,
            ) : ModifierAnimation() {

                private val startValue = when (directionNav) {
                    Direction.Nav.Push -> 1.0f
                    Direction.Nav.Pop -> 0.0f
                }
                private val endValue = when (directionNav) {
                    Direction.Nav.Push -> 0.0f
                    Direction.Nav.Pop -> 1.0f
                }

                private val entranceFactor = when (config.entrance) {
                    Entrance.FromEnd -> 1.0f
                    Entrance.FromStart -> -1.0f
                }

                @Composable
                override fun Modifier.animate(boundaries:Size): Modifier {
                    val progress = animationProgress.animateFloat(
                        startValue = startValue,
                        endValue = endValue,
                        animationSpecToEnd = tween(
                            durationMillis = config.duration_ms,
                            easing = LinearEasing
                        )
                    )
                    val width = boundaries.width * entranceFactor
                    return this
                        .offset(x = (width * progress.value).dp, y = 0.dp)
                }
            }

            internal class Out(
                private val config: Config.Type.SlideHorizontal,
                private val animationProgress: AnimationProgress,
                directionNav: Direction.Nav,
            ) : ModifierAnimation() {

                private val startValue = when (directionNav) {
                    Direction.Nav.Push -> 0.0f
                    Direction.Nav.Pop -> -1.0f
                }
                private val endValue = when (directionNav) {
                    Direction.Nav.Push -> -1.0f
                    Direction.Nav.Pop -> 0.0f
                }

                private val entranceFactor = when (config.entrance) {
                    Entrance.FromEnd -> 1.0f
                    Entrance.FromStart -> -1.0f
                }

                @Composable
                override fun Modifier.animate(boundaries:Size): Modifier {
                    val progress = animationProgress.animateFloat(
                        startValue = startValue,
                        endValue = endValue,
                        animationSpecToEnd = tween(
                            durationMillis = config.duration_ms,
                            easing = LinearEasing
                        )
                    )

                    val width = when (config.effect) {
                        Effect.CoverPush -> {
                            (boundaries.width * entranceFactor).dp / 2
                        }
                        Effect.Push -> {
                            (boundaries.width * entranceFactor).dp
                        }
                        Effect.Cover -> {
                            0.dp
                        }
                    }
                    return this
                        .offset(x = width * progress.value, y = 0.dp)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                color = Color.Black,
                                alpha = -progress.value * config.outDarkAlphaFactor,
                                topLeft = Offset(0f, 0f),
                                size = size,
                                style = Fill
                            )
                        }
                }
            }

        }

        object Vertical {

            internal const val DURATION_ms = 300
            internal const val OUT_DARK_ALPHA_FACTOR = 0.60f

            enum class Entrance { FromBottom, FromTop }

            internal fun AnimationProgress.slideVertical(
                config: Config.Type.SlideVertical,
                directionNav: Direction.Nav,
                directionContent: Direction.Content,
            ): ModifierAnimation {
                return when (directionContent) {
                    Direction.Content.Enter -> when (directionNav) {
                        Direction.Nav.Push -> In(config, this, Direction.Nav.Push)
                        Direction.Nav.Pop -> Out(config, this, Direction.Nav.Pop)
                    }
                    Direction.Content.Exit -> when (directionNav) {
                        Direction.Nav.Push -> Out(config, this, Direction.Nav.Push)
                        Direction.Nav.Pop -> In(config, this, Direction.Nav.Pop)
                    }
                }
            }

            internal class In(
                private val config: Config.Type.SlideVertical,
                private val animationProgress: AnimationProgress,
                directionNav: Direction.Nav,
            ) : ModifierAnimation() {

                private val startValue = when (directionNav) {
                    Direction.Nav.Push -> 1.0f
                    Direction.Nav.Pop -> 0.0f
                }
                private val endValue = when (directionNav) {
                    Direction.Nav.Push -> 0.0f
                    Direction.Nav.Pop -> 1.0f
                }

                private val entranceFactor = when (config.entrance) {
                    Entrance.FromBottom -> 1.0f
                    Entrance.FromTop -> -1.0f
                }

                @Composable
                override fun Modifier.animate(boundaries:Size): Modifier {
                    val progress = animationProgress.animateFloat(
                        startValue = startValue,
                        endValue = endValue,
                        animationSpecToEnd = tween(
                            durationMillis = config.duration_ms,
                            easing = LinearEasing
                        )
                    )
                    val height = boundaries.height * entranceFactor
                    return this
                        .offset(x = 0.dp, y = (height * progress.value).dp)
                }
            }

            internal class Out(
                private val config: Config.Type.SlideVertical,
                private val animationProgress: AnimationProgress,
                directionNav: Direction.Nav,
            ) : ModifierAnimation() {

                private val startValue = when (directionNav) {
                    Direction.Nav.Push -> 0.0f
                    Direction.Nav.Pop -> -1.0f
                }
                private val endValue = when (directionNav) {
                    Direction.Nav.Push -> -1.0f
                    Direction.Nav.Pop -> 0.0f
                }

                private val entranceFactor = when (config.entrance) {
                    Entrance.FromBottom -> 1.0f
                    Entrance.FromTop -> -1.0f
                }

                @Composable
                override fun Modifier.animate(boundaries:Size): Modifier {
                    val progress = animationProgress.animateFloat(
                        startValue = startValue,
                        endValue = endValue,
                        animationSpecToEnd = tween(
                            durationMillis = config.duration_ms,
                            easing = LinearEasing
                        )
                    )
                    val height = when (config.effect) {
                        Effect.CoverPush -> {
                            (boundaries.height * entranceFactor).dp / 4
                        }
                        Effect.Push -> {
                            (boundaries.height * entranceFactor).dp
                        }
                        Effect.Cover -> {
                            0.dp
                        }
                    }
                    return this
                        .offset(x = 0.dp, y = height * progress.value)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                color = Color.Black,
                                alpha = -progress.value * config.outDarkAlphaFactor,
                                topLeft = Offset(0f, 0f),
                                size = size,
                                style = Fill
                            )
                        }
                }
            }

        }

    }

}
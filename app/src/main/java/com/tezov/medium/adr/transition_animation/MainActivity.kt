package com.tezov.medium.adr.transition_animation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tezov.medium.adr.transition_animation.ui.theme.Transition_animationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Transition_animationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Screens()
                }
            }
        }
    }
}

@Composable
fun Screens() {
    val screens = remember {
        listOf(
            AnimationContent(
                id = "screen_A",
                animationConfig = Animations.Config(
                    default = Animations.Config.Type.SlideHorizontal(
                        duration_ms = 500
                    )
                ),
                content = {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Blue)) {

                    }
                }
            ),
            AnimationContent(
                id = "screen_B",
                animationConfig = Animations.Config(
                    default = Animations.Config.Type.SlideHorizontal(
                        duration_ms = 500
                    )
                ),
                content = {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red)) {

                    }
                }
            ),
            AnimationContent(
                id = "screen_C",
                animationConfig = Animations.Config(
                    default = Animations.Config.Type.SlideHorizontal(
                        duration_ms = 500
                    )
                ),
                content = {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray)) {

                    }
                }
            ),
            AnimationContent(
                id = "screen_D",
                animationConfig = Animations.Config(
                    default = Animations.Config.Type.SlideHorizontal(
                        duration_ms = 500
                    )
                ),
                content = {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Green)) {

                    }
                }
            )
        )
    }

    val showId = remember {
        mutableStateOf("screen_A")
    }
    val isNavigatingBack = remember {
        mutableStateOf(false)
    }

    AnimateNavigation(
        screens = screens,
        showId = showId.value,
        isNavigatingBack = isNavigatingBack.value
    )

    LaunchedEffect(Unit) {
        repeat(10) {
            delay(2000)
            isNavigatingBack.value = false
            showId.value = "screen_B"

            delay(2000)
            isNavigatingBack.value = false
            showId.value = "screen_C"

            delay(2000)
            isNavigatingBack.value = false
            showId.value = "screen_D"

            delay(2000)
            isNavigatingBack.value = true
            showId.value = "screen_C"

            delay(2000)
            isNavigatingBack.value = true
            showId.value = "screen_B"

            delay(2000)
            isNavigatingBack.value = true
            showId.value = "screen_A"
        }

    }

}



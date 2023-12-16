package com.tezov.medium.adr.transition_animation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Preview
import com.tezov.medium.adr.transition_animation.NavigationAnimation.Config.*
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
            Content(
                id = "screen_A",
                animationConfig = NavigationAnimation.Config {

                },
                content = {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Blue)) {

                    }
                }
            ),
            Content(
                id = "screen_B",
                animationConfig = NavigationAnimation.Config {

                },
                content = {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red)) {

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

    animate(
        screens = screens,
        showId = showId.value,
        isNavigatingBack = isNavigatingBack.value
    )

    LaunchedEffect(Unit) {
        delay(1000)
        showId.value = "screen_B"
    }

}



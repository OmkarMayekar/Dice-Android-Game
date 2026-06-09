package com.greencode27.dice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.greencode27.dice.ui.DiceGameScreen
import com.greencode27.dice.ui.DiceGameViewModel
import com.greencode27.dice.ui.SplashScreen
import com.greencode27.dice.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var showSplash by remember { mutableStateOf(true) }

        Crossfade(
          targetState = showSplash,
          animationSpec = tween(durationMillis = 600),
          label = "SplashTransition"
        ) { isSplashActive ->
          if (isSplashActive) {
            SplashScreen(
              onSplashFinished = { showSplash = false }
            )
          } else {
            val viewModel: DiceGameViewModel = viewModel()
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
              DiceGameScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
              )
            }
          }
        }
      }
    }
  }
}

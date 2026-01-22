package com.example.checkersgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import com.example.checkersgame.presentation.core.Config
import com.example.checkersgame.presentation.navigation.CheckersNavGraph
import com.example.checkersgame.ui.theme.BackgroundColor
import com.example.checkersgame.ui.theme.BoardBrownDark
import com.example.checkersgame.ui.theme.CardColor
import com.example.checkersgame.ui.theme.PieceGuestColor

class MainActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      android.util.Log.e("MY_DEBUG", "------------------------------------")
      android.util.Log.e("MY_DEBUG", "MY URL IS: ${Config.HOST_URL}")
      android.util.Log.e("MY_DEBUG", "------------------------------------")
      setContent {
         MaterialTheme(
            colorScheme = lightColorScheme(
               primary = BoardBrownDark,
               secondary = PieceGuestColor,
               background = BackgroundColor,
               surface = CardColor
            )
         ) {
            Surface(
               modifier = Modifier.fillMaxSize(),
               color = MaterialTheme.colorScheme.background
            ) {
               CheckersNavGraph()
            }
         }
      }
   }
}
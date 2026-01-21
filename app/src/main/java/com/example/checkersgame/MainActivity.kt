package com.example.checkersgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Modifier

import com.example.checkersgame.presentation.core.enums.Screen
import com.example.checkersgame.presentation.screens.GameScreen
import com.example.checkersgame.presentation.screens.HistoryScreen
import com.example.checkersgame.presentation.screens.LobbyScreen
import com.example.checkersgame.presentation.screens.LoginScreen
import com.example.checkersgame.ui.theme.BackgroundColor
import com.example.checkersgame.ui.theme.BoardBrownDark

import com.example.checkersgame.ui.theme.CardColor

import com.example.checkersgame.ui.theme.PieceGuestColor
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*


const val TAG = "CheckersDebug"

class MainActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContent {
         MaterialTheme(
            colorScheme = lightColorScheme(
               primary = BoardBrownDark,
               secondary = PieceGuestColor,
               background = BackgroundColor,
               surface = CardColor
            )
         ) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
               CheckersApp()
            }
         }
      }
   }
}

val client = HttpClient(CIO) {
   install(WebSockets) {
      pingInterval = 10_000
   }
   install(ContentNegotiation) { json() }
   defaultRequest { header("ngrok-skip-browser-warning", "true") }
}





@Composable
fun CheckersApp() {
   var userId by remember { mutableStateOf<Int?>(null) }
   var activeGameId by remember { mutableStateOf<Int?>(null) }
   var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

   LaunchedEffect(userId, activeGameId) {
      currentScreen = when {
         userId == null -> Screen.LOGIN
         activeGameId != null -> Screen.GAME
         else -> if (currentScreen == Screen.HISTORY) Screen.HISTORY else Screen.LOBBY
      }
   }

   when (currentScreen) {
      Screen.LOGIN -> LoginScreen { id -> userId = id }
      Screen.LOBBY -> LobbyScreen(
         userId = userId!!,
         onJoin = { gameId -> activeGameId = gameId },
         onHistory = { currentScreen = Screen.HISTORY }
      )
      Screen.HISTORY -> HistoryScreen(userId!!) { currentScreen = Screen.LOBBY }
      Screen.GAME -> GameScreen(userId!!, activeGameId!!) {
         activeGameId = null
         currentScreen = Screen.LOBBY
      }
   }
}










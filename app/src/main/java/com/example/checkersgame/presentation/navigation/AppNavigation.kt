package com.example.checkersgame.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.checkersgame.presentation.core.enums.Screen
import com.example.checkersgame.presentation.screens.GameScreen
import com.example.checkersgame.presentation.screens.HistoryScreen
import com.example.checkersgame.presentation.screens.LobbyScreen
import com.example.checkersgame.presentation.screens.LoginScreen

@Composable
fun CheckersNavGraph() {
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
package com.example.checkersgame.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.checkersgame.presentation.core.TokenManager
import com.example.checkersgame.presentation.core.enums.Screen
import com.example.checkersgame.presentation.screens.GameScreen
import com.example.checkersgame.presentation.screens.HistoryScreen
import com.example.checkersgame.presentation.screens.LobbyScreen
import com.example.checkersgame.presentation.screens.LoginScreen

@Composable
fun CheckersNavGraph() {
   // Global state for the app navigation
   var userId by remember { mutableStateOf<Int?>(null) }
   var activeGameId by remember { mutableStateOf<Int?>(null) }
   var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

   val context = LocalContext.current

   // 1. Auto-Login: Check if token exists on app start
   LaunchedEffect(Unit) {
      val token = TokenManager.getToken()
      if (token != null) {
         userId = TokenManager.getUserId()
         currentScreen = Screen.LOBBY // Skip login screen
      }
   }

   // 2. Auto-Navigation: React to state changes (Login success or Game Join)
   LaunchedEffect(userId, activeGameId) {
      if (userId != null && activeGameId == null && currentScreen == Screen.LOGIN) {
         currentScreen = Screen.LOBBY
      } else if (activeGameId != null) {
         currentScreen = Screen.GAME
      }
   }

   // 3. Screen Router
   when (currentScreen) {
      Screen.LOGIN -> LoginScreen { id ->
         userId = id; currentScreen = Screen.LOBBY
      }

      Screen.LOBBY -> LobbyScreen(
         userId = userId!!,
         onJoin = { gameId -> activeGameId = gameId }, // Set ID to trigger navigation
         onHistory = { currentScreen = Screen.HISTORY }
      )

      Screen.HISTORY -> HistoryScreen(userId!!) {
         currentScreen = Screen.LOBBY // Back button action
      }

      Screen.GAME -> GameScreen(userId!!, activeGameId!!) {
         activeGameId = null // Clear game ID on exit
         currentScreen = Screen.LOBBY
      }
   }
}
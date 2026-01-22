package com.example.checkersgame.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkersgame.data.KtorClient.client
import com.example.checkersgame.presentation.core.Config
import com.example.checkersgame.data.models.GameLobbyItem
import com.example.checkersgame.presentation.components.GameCard
import com.example.checkersgame.ui.theme.BackgroundColor
import com.example.checkersgame.ui.theme.BoardBrownDark
import com.example.checkersgame.ui.theme.PieceHostColor
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(userId: Int, onJoin: (Int) -> Unit, onHistory: () -> Unit) {
   var games by remember { mutableStateOf(listOf<GameLobbyItem>()) }
   val scope = rememberCoroutineScope()
   val context = LocalContext.current

   // Polling Loop: Refreshes the game list every 3 seconds
   LaunchedEffect(Unit) {
      while(isActive) {
         try {
            // Token is auto-injected by KtorClient
            val response = client.get("${Config.HOST_URL}/lobby")
            games = Json.decodeFromString(response.bodyAsText())
         } catch (e: Exception) {}
         delay(3000)
      }
   }

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text("Активні ігри", fontWeight = FontWeight.Bold) },
            actions = { IconButton(onClick = onHistory) { Icon(Icons.Default.List, "History", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BoardBrownDark, titleContentColor = Color.White)
         )
      },
      floatingActionButton = {
         // Create New Game Button
         ExtendedFloatingActionButton(
            onClick = {
               scope.launch {
                  try {
                     // POST creates game -> returns ID -> Navigate to GameScreen
                     val response = client.post("${Config.HOST_URL}/create")
                     val gameId = response.bodyAsText().substringAfter("gameId\":").substringBefore("}").toInt()
                     onJoin(gameId)
                  } catch (e: Exception) { Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show() }
               }
            },
            containerColor = PieceHostColor, contentColor = Color.White, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Створити") }
         )
      }
   ) { padding ->
      Column(modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundColor)) {

         // Empty State or List State
         if (games.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
               Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                  Text("Немає ігор. Створіть нову!", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
               }
            }
         } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
               items(games) { game -> GameCard(game, onJoin) }
            }
         }
      }
   }
}
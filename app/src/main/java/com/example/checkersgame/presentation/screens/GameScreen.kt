package com.example.checkersgame.presentation.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checkersgame.data.KtorClient
import com.example.checkersgame.data.models.GameState
import com.example.checkersgame.data.models.MoveRequest
import com.example.checkersgame.presentation.components.BoardGrid
import com.example.checkersgame.presentation.components.BoardLetters
import com.example.checkersgame.presentation.components.BoardNumbers
import com.example.checkersgame.presentation.core.Config
import com.example.checkersgame.ui.theme.BackgroundColor
import com.example.checkersgame.ui.theme.BoardBrownDark
import com.example.checkersgame.ui.theme.PieceGuestColor
import com.example.checkersgame.ui.theme.PieceHostColor
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(userId: Int, gameId: Int, onExit: () -> Unit) {
   var board by remember { mutableStateOf<List<List<Int>>>(emptyList()) }
   var turnId by remember { mutableStateOf(0) }
   var opponentId by remember { mutableStateOf<Int?>(null) }
   var hostId by remember { mutableStateOf(0) }
   var winnerId by remember { mutableStateOf<Int?>(null) }
   var rematchCount by remember { mutableStateOf(0) }
   var isOpponentConnected by remember { mutableStateOf(true) }

   var statusText by remember { mutableStateOf("Підключення...") }
   var wsSession by remember { mutableStateOf<DefaultClientWebSocketSession?>(null) }
   var connectionKey by remember { mutableIntStateOf(0) }

   val scope = rememberCoroutineScope()
   val context = LocalContext.current

   LaunchedEffect(isOpponentConnected) {
      if (!isOpponentConnected && opponentId != null) {
         Toast.makeText(context, "Суперник втратив зв'язок!", Toast.LENGTH_LONG).show()
      } else if (isOpponentConnected && opponentId != null) {
         Toast.makeText(context, "Суперник повернувся!", Toast.LENGTH_SHORT).show()
      }
   }

   LaunchedEffect(gameId, connectionKey) {
      while (isActive) {
         try {
            val wsUrl = Config.HOST_URL.replace("http", "ws").replace("https", "wss")
            KtorClient.client.webSocket("$wsUrl/game/$gameId?userId=$userId") {
               wsSession = this
               statusText = "Підключено!"
               for (frame in incoming) {
                  if (frame is Frame.Text) {
                     try {
                        val state = Json.decodeFromString<GameState>(frame.readText())
                        board = state.board
                        turnId = state.turnPlayerId
                        opponentId = state.player2Id
                        hostId = state.player1Id
                        winnerId = state.winnerId
                        rematchCount = state.rematchRequests
                        isOpponentConnected = state.isOpponentConnected

                        statusText = when {
                           opponentId == null -> "Очікуємо суперника..."
                           !isOpponentConnected -> "СУПЕРНИК ВІДКЛЮЧИВСЯ"
                           winnerId != null -> if(winnerId == userId) "ПЕРЕМОГА!" else "ГРУ ЗАВЕРШЕНО"
                           turnId == userId -> "ВАШ ХІД"
                           else -> "Хід суперника..."
                        }
                     } catch (e: Exception) { e.printStackTrace() }
                  }
               }
            }
         } catch (e: Exception) {
            statusText = "Відновлення з'єднання..."
            delay(3000)
         }
      }
   }


   if (winnerId != null) {
      AlertDialog(
         onDismissRequest = {},
         title = { Text(if (winnerId == userId) "🎉 ПЕРЕМОГА!" else "😢 ПОРАЗКА") },
         text = {
            Column {
               Text(if (winnerId == userId) "Вітаємо! Ви перемогли." else "Пощастить наступного разу.")
               Spacer(Modifier.height(16.dp))
               if (rematchCount > 0) Text("Опонент пропонує реванш!", color = BoardBrownDark, fontWeight = FontWeight.Bold)
               else Text("Бажаєте зіграти ще раз?")
            }
         },
         confirmButton = {
            Button(
               onClick = { scope.launch { wsSession?.send(Frame.Text("REMATCH")) } },
               colors = ButtonDefaults.buttonColors(containerColor = PieceHostColor)
            ) { Text(if(rematchCount > 0) "Прийняти" else "Реванш") }
         },
         dismissButton = { Button(onClick = onExit, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Меню") } }
      )
   }

   Scaffold(
      topBar = {
         TopAppBar(
            title = {

               Column {
                  Text("Гра #$gameId", fontWeight = FontWeight.Bold)
                  if (!isOpponentConnected && opponentId != null) {
                     Text("Опонент офлайн", fontSize = 12.sp, color = Color.Red)
                  }
               }
            },
            actions = {
               IconButton(onClick = {
                  Toast.makeText(context, "Перепідключення...", Toast.LENGTH_SHORT).show()
                  wsSession?.cancel()
                  connectionKey++
               }) { Icon(Icons.Default.Refresh, "Refresh", tint = Color.White) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BoardBrownDark, titleContentColor = Color.White)
         )
      }
   ) { padding ->
      Column(
         Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(padding)
            .padding(16.dp),
         horizontalAlignment = Alignment.CenterHorizontally,
         verticalArrangement = Arrangement.Center
      ) {

         val statusColor by animateColorAsState(targetValue = when {
            !isOpponentConnected && opponentId != null -> Color.Red
            statusText == "ВАШ ХІД" -> Color(0xFF4CAF50)
            statusText.contains("Відновлення") -> Color.Red
            statusText.contains("Очікуємо") -> Color(0xFFFF9800)
            statusText.contains("Хід суперника") -> PieceGuestColor
            else -> BoardBrownDark
         }, label = "color")


         Surface(
            color = statusColor,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(bottom = 24.dp).shadow(6.dp, RoundedCornerShape(24.dp))
         ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)) {

               if (!isOpponentConnected && opponentId != null) {
                  Icon(Icons.Default.Warning, null, tint = Color.White)
                  Spacer(Modifier.width(8.dp))
               }
               Text(
                  text = statusText,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp
               )
            }
         }

         if (board.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.shadow(12.dp).background(BoardBrownDark).padding(8.dp)) {
               val isHost = userId == hostId
               BoardLetters(isHost)
               Row {
                  BoardNumbers(isHost)
                  Box(modifier = Modifier.size(300.dp).border(2.dp, Color.Black)) {
                     BoardGrid(board, isHost) { fx, fy, tx, ty ->
                        if (winnerId != null) return@BoardGrid

                        if (opponentId != null && !isOpponentConnected) {
                           Toast.makeText(context, "Зачекайте повернення суперника!", Toast.LENGTH_SHORT).show()
                           return@BoardGrid
                        }

                        if (opponentId == null) {
                           Toast.makeText(context, "Чекаємо другого гравця...", Toast.LENGTH_SHORT).show()
                        } else if (turnId == userId) {
                           scope.launch { wsSession?.send(Frame.Text(Json.encodeToString(MoveRequest(fx, fy, tx, ty)))) }
                        } else {
                           Toast.makeText(context, "Зачекайте...", Toast.LENGTH_SHORT).show()
                        }
                     }

                     if (opponentId != null && !isOpponentConnected) {
                        Box(
                           modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                           contentAlignment = Alignment.Center
                        ) {
                           Text("ПАУЗА", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 32.sp)
                        }
                     }
                  }
                  BoardNumbers(isHost)
               }
               BoardLetters(isHost)
            }
         } else { CircularProgressIndicator(color = BoardBrownDark) }

         Spacer(Modifier.height(32.dp))
         Button(onClick = onExit, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.fillMaxWidth(0.6f), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.ExitToApp, null)
            Spacer(Modifier.width(8.dp))
            Text("Покинути гру")
         }
      }
   }
}
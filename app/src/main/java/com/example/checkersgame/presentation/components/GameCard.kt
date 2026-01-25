package com.example.checkersgame.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checkersgame.data.models.GameLobbyItem
import com.example.checkersgame.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun GameCard(game: GameLobbyItem, onJoin: (Int) -> Unit) {
   // Check if the game is scheduled for deletion (host left)
   val isDying = game.destroyAt != null
   var timeLeft by remember { mutableLongStateOf(0L) }

   // Countdown timer logic for abandoned games
   if (isDying) {
      LaunchedEffect(game.destroyAt) {
         while (true) {
            val diff = (game.destroyAt!! - System.currentTimeMillis()) / 1000
            timeLeft = if (diff > 0) diff else 0
            delay(1000)
         }
      }
   }

   // Styling based on state (Red background if dying, Highlight border if my game)
   val cardColor = if (isDying) Color(0xFFFFEBEE) else Color.White
   val borderColor = if (game.isMyGame) HighlightColor else Color.Transparent

   Card(
      modifier = Modifier
         .fillMaxWidth()
         .padding(horizontal = 4.dp)
         .border(2.dp, borderColor, RoundedCornerShape(16.dp))
         .clickable { onJoin(game.gameId) },
      elevation = CardDefaults.cardElevation(6.dp),
      colors = CardDefaults.cardColors(containerColor = cardColor),
      shape = RoundedCornerShape(16.dp)
   ) {
      Column(modifier = Modifier.padding(16.dp)) {

         // --- Header Row (Avatar + Name) ---
         Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
               shape = CircleShape,
               color = BoardBrownLight,
               modifier = Modifier.size(48.dp)
            ) {
               Box(contentAlignment = Alignment.Center) {
                  Text(
                     text = game.hostName.take(1).uppercase(),
                     fontWeight = FontWeight.Bold,
                     color = BoardBrownDark,
                     fontSize = 20.sp
                  )
               }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
               Text(
                  text = game.hostName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp,
                  color = BoardBrownDark
               )
               Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                  Spacer(Modifier.width(4.dp))
                  Text(text = "${game.playerCount}/2 гравців", fontSize = 13.sp, color = Color.Gray)
               }
            }

            if (game.isMyGame) {
               Badge(containerColor = HighlightColor) {
                  Text("ВАША ГРА", modifier = Modifier.padding(4.dp))
               }
            }
         }

         // --- Footer Section ---
         if (isDying) {
            // Warning view for abandoned games
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.Red.copy(alpha = 0.2f))
            Spacer(Modifier.height(8.dp))

            Row(
               verticalAlignment = Alignment.CenterVertically,
               modifier = Modifier
                  .fillMaxWidth()
                  .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                  .padding(8.dp)
            ) {
               Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(20.dp))
               Spacer(Modifier.width(8.dp))
               Column {
                  Text("Всі гравці вийшли!", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                  Text(
                     text = "Знищення через: ${formatTime(timeLeft)}",
                     fontSize = 14.sp,
                     color = Color.Red,
                     fontWeight = FontWeight.Black
                  )
               }
            }
         } else {
            // Action button for active games
            Spacer(Modifier.height(12.dp))
            Button(
               onClick = { onJoin(game.gameId) },
               modifier = Modifier.fillMaxWidth().height(40.dp),
               colors = ButtonDefaults.buttonColors(containerColor = BoardBrownDark),
               shape = RoundedCornerShape(8.dp)
            ) {
               Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp))
               Spacer(Modifier.width(8.dp))
               Text(if(game.isMyGame) "Повернутися" else "Приєднатися")
            }
         }
      }
   }
}

// Helper to format seconds into MM:SS
fun formatTime(seconds: Long): String {
   val m = seconds / 60
   val s = seconds % 60
   return "%02d:%02d".format(m, s)
}
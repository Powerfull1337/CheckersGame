package com.example.checkersgame.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checkersgame.models.GameLobbyItem
import com.example.checkersgame.ui.theme.BoardBrownDark
import com.example.checkersgame.ui.theme.BoardBrownLight
import com.example.checkersgame.ui.theme.HighlightColor

@Composable
fun GameCard(game: GameLobbyItem, onJoin: (Int) -> Unit) {
   val btnColor = if (game.isMyGame) HighlightColor else BoardBrownDark
   val btnText = if (game.isMyGame) "Повернутися" else "Грати"
   val textColor = if(game.isMyGame) Color.Black else Color.White

   Card(
      modifier = Modifier.fillMaxWidth().clickable { onJoin(game.gameId) },
      elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)
   ) {
      Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
         Surface(shape = CircleShape, color = BoardBrownLight, modifier = Modifier.size(50.dp)) {
            Box(contentAlignment = Alignment.Center) {
               Text(game.hostName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = BoardBrownDark, fontSize = 22.sp)
            }
         }
         Spacer(Modifier.width(16.dp))
         Column {
            Text(text = game.hostName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = if(game.isMyGame) "Ваша гра" else "Очікує опонента", color = Color.Gray, fontSize = 14.sp)
         }
         Spacer(Modifier.weight(1f))
         Button(
            onClick = { onJoin(game.gameId) },
            colors = ButtonDefaults.buttonColors(containerColor = btnColor, contentColor = textColor),
            shape = RoundedCornerShape(8.dp)
         ) {
            Text(btnText)
            if (!game.isMyGame) { Spacer(Modifier.width(4.dp)); Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp)) }
         }
      }
   }
}
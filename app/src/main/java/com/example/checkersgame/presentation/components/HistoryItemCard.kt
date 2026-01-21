package com.example.checkersgame.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checkersgame.models.HistoryItem
import com.example.checkersgame.parseMoveHistory
import com.example.checkersgame.ui.theme.BoardBrownDark
import com.example.checkersgame.ui.theme.HighlightColor

@Composable
fun HistoryItemCard(item: HistoryItem, currentUserId: Int) {
   var expanded by remember { mutableStateOf(false) }
   val parsedMoves = remember(item.moves) { parseMoveHistory(item.moves) }

   Card(
      modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.shadow(4.dp, RoundedCornerShape(12.dp)),
      colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)
   ) {
      Column(Modifier.padding(16.dp)) {
         Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
               if (item.winner == "В процесі") Icons.Default.PlayArrow else Icons.Default.Star, null,
               tint = if (item.winner == "В процесі") Color.Gray else HighlightColor, modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
               Text("Проти: ${item.opponentName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
               Text("Переможець: ${item.winner}", fontSize = 14.sp, color = if (item.winner == "В процесі") Color.Gray else BoardBrownDark)
            }
            Spacer(Modifier.weight(1f))
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
         }
         if (expanded) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text("Хронологія:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BoardBrownDark)
            Spacer(Modifier.height(8.dp))
            if (parsedMoves.isEmpty()) Text("Ходів не було", fontSize = 13.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
            else Column {
               parsedMoves.forEach { move ->
                  Text(text = move, fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = Color.DarkGray, modifier = Modifier.padding(vertical = 2.dp))
               }
            }
         }
      }
   }
}
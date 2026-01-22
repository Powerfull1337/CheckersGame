package com.example.checkersgame.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checkersgame.data.models.HistoryItem
import com.example.checkersgame.presentation.util.parseMoveHistory
import com.example.checkersgame.ui.theme.BoardBrownDark
import com.example.checkersgame.ui.theme.HighlightColor
import com.example.checkersgame.ui.theme.PieceGuestColor
import com.example.checkersgame.ui.theme.PieceHostColor

@Composable
fun HistoryItemCard(item: HistoryItem, currentUserId: Int) {
   var expanded by remember { mutableStateOf(false) }

   val parsedMoves = remember(item.moves) { parseMoveHistory(item.moves) }


   val statusColor = if (item.winner == "В процесі") Color.Gray else HighlightColor
   val statusIcon = if (item.winner == "В процесі") Icons.Default.PlayArrow else Icons.Default.Star

   Card(
      modifier = Modifier
         .fillMaxWidth()
         .padding(vertical = 4.dp)
         .shadow(4.dp, RoundedCornerShape(16.dp))
         .clickable { expanded = !expanded },
      colors = CardDefaults.cardColors(containerColor = Color.White),
      shape = RoundedCornerShape(16.dp)
   ) {
      Column(Modifier.padding(16.dp)) {

         Row(verticalAlignment = Alignment.CenterVertically) {

            Surface(
               shape = CircleShape,
               color = statusColor.copy(alpha = 0.1f),
               modifier = Modifier.size(40.dp)
            ) {
               Box(contentAlignment = Alignment.Center) {
                  Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(24.dp))
               }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
               Text(
                  text = "Проти: ${item.opponentName}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = BoardBrownDark
               )
               Text(
                  text = if(item.winner == "В процесі") "Гра не завершена" else "Переміг: ${item.winner}",
                  fontSize = 13.sp,
                  color = if (item.winner == "В процесі") Color.Gray else Color(0xFF4CAF50),
                  fontWeight = if(item.winner != "В процесі") FontWeight.SemiBold else FontWeight.Normal
               )
            }

            Icon(
               imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
               contentDescription = null,
               tint = Color.Gray
            )
         }


         if (expanded) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))

            Text(
               "Хронологія ходів:",
               fontWeight = FontWeight.Bold,
               fontSize = 14.sp,
               color = BoardBrownDark,
               modifier = Modifier.padding(bottom = 8.dp)
            )

            if (parsedMoves.isEmpty()) {
               Text(
                  "Ходів не було",
                  fontSize = 13.sp,
                  fontStyle = FontStyle.Italic,
                  color = Color.Gray,
                  modifier = Modifier.padding(start = 8.dp)
               )
            } else {
               Column(
                  modifier = Modifier
                     .fillMaxWidth()
                     .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                     .clip(RoundedCornerShape(8.dp))
               ) {
                  parsedMoves.forEachIndexed { index, move ->

                     val bgColor = if (index % 2 == 0) Color(0xFFF5F5F5) else Color.White


                     val pieceColor = if (move.isHostMove) PieceHostColor else PieceGuestColor
                     val playerLabel = if (move.isHostMove) "Хост" else "Гість"

                     Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                           .fillMaxWidth()
                           .background(bgColor)
                           .padding(horizontal = 12.dp, vertical = 8.dp)
                     ) {

                        Text(
                           text = "${move.number}.",
                           fontSize = 12.sp,
                           color = Color.Gray,
                           modifier = Modifier.width(24.dp)
                        )


                        Box(
                           modifier = Modifier
                              .size(12.dp)
                              .background(pieceColor, CircleShape)
                              .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                        )

                        Spacer(Modifier.width(8.dp))


                        Text(
                           text = move.text,
                           fontSize = 14.sp,
                           fontFamily = FontFamily.Monospace,
                           fontWeight = FontWeight.Medium,
                           color = BoardBrownDark
                        )

                        Spacer(Modifier.weight(1f))


                        Text(
                           text = playerLabel,
                           fontSize = 10.sp,
                           color = Color.Gray
                        )
                     }
                  }
               }
            }
         }
      }
   }
}
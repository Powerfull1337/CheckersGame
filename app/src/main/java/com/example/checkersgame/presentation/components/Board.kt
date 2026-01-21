package com.example.checkersgame.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checkersgame.ui.theme.BoardBrownDark
import com.example.checkersgame.ui.theme.BoardBrownLight
import com.example.checkersgame.ui.theme.HighlightColor
import com.example.checkersgame.ui.theme.PieceGuestColor
import com.example.checkersgame.ui.theme.PieceHostColor

@Composable
fun BoardGrid(board: List<List<Int>>, isHost: Boolean, onMove: (Int, Int, Int, Int) -> Unit) {
   var selX by remember { mutableStateOf<Int?>(null) }
   var selY by remember { mutableStateOf<Int?>(null) }

   Column(Modifier.fillMaxSize()) {
      val yRange = if (isHost) 0..7 else 7 downTo 0
      val xRange = if (isHost) 0..7 else 7 downTo 0

      for (y in yRange) {
         Row(Modifier.weight(1f)) {
            for (x in xRange) {
               val piece = board[y][x]
               val isBlackCell = (x + y) % 2 != 0
               val isSelected = selX == x && selY == y
               val bgColor = when {
                  isSelected -> HighlightColor
                  isBlackCell -> BoardBrownDark
                  else -> BoardBrownLight
               }

               Box(
                  modifier = Modifier.weight(1f).fillMaxHeight().background(bgColor).clickable(enabled = isBlackCell) {
                     if (selX != null && piece == 0) {
                        onMove(selX!!, selY!!, x, y)
                        selX = null; selY = null
                     } else if (piece != 0) {
                        val isMyPiece = (isHost && piece == 1) || (!isHost && piece == 2)
                        if (isMyPiece) { selX = x; selY = y }
                     }
                  }, contentAlignment = Alignment.Center
               ) {
                  if (piece != 0) CheckersPiece(if (piece == 1) PieceHostColor else PieceGuestColor)
               }
            }
         }
      }
   }
}
@Composable
fun CheckersPiece(color: Color) {
   Box(
      modifier = Modifier.fillMaxSize(0.85f).shadow(6.dp, CircleShape)
         .background(brush = Brush.radialGradient(colors = listOf(color.copy(alpha = 0.8f), color)), shape = CircleShape)
         .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
      contentAlignment = Alignment.Center
   ) {
      Box(modifier = Modifier.fillMaxSize(0.6f).border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape))
   }
}
@Composable
fun BoardLetters(isHost: Boolean) {
   Row(Modifier.width(300.dp)) {
      val range = if (isHost) 0..7 else 7 downTo 0
      for (i in range) {
         Text(text = "${(i + 65).toChar()}", color = BoardBrownLight, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
   }
}

@Composable
fun BoardNumbers(isHost: Boolean) {
   Column(Modifier.height(300.dp)) {
      val range = if (isHost) 0..7 else 7 downTo 0
      for (i in range) {
         Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(text = "${i + 1}", color = BoardBrownLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
         }
      }
   }
}
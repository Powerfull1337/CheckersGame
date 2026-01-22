package com.example.checkersgame.presentation.util

data class ParsedMove(
   val number: Int,
   val text: String,
   val isHostMove: Boolean
)

fun parseMoveHistory(rawMoves: String): List<ParsedMove> {
   if (rawMoves.isBlank()) return emptyList()

   return rawMoves.split(";")
      .filter { it.isNotBlank() }
      .mapIndexed { index, moveStr ->
         try {

            val parts = moveStr.split("->")
            val fromCoords = parts[0].split(",").map { it.toInt() }
            val toCoords = parts[1].split(",").map { it.toInt() }


            val from = formatCoordinate(fromCoords[0], fromCoords[1])
            val to = formatCoordinate(toCoords[0], toCoords[1])


            val isHost = index % 2 == 0

            ParsedMove(
               number = index + 1,
               text = "$from ➝ $to",
               isHostMove = isHost
            )
         } catch (e: Exception) {
            ParsedMove(index + 1, "Помилка запису", true)
         }
      }
}


private fun formatCoordinate(x: Int, y: Int): String {
   val letter = (x + 65).toChar()
   val number = y + 1
   return "$letter$number"
}
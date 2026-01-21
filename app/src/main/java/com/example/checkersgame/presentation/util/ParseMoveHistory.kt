package com.example.checkersgame.presentation.util

fun parseMoveHistory(rawMoves: String): List<String> {
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
            "${index + 1}. $from ➝ $to"
         } catch (e: Exception) { "Error" }
      }
}

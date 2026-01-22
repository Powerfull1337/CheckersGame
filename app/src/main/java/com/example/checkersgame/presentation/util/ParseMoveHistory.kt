package com.example.checkersgame.presentation.util

// Model for a single move row in the history UI
data class ParsedMove(val number: Int, val text: String, val isHostMove: Boolean)

// Converts raw DB string (e.g. "0,2->1,3;5,5->4,4;") into a list of readable objects
fun parseMoveHistory(rawMoves: String): List<ParsedMove> {
   if (rawMoves.isBlank()) return emptyList()

   return rawMoves.split(";")
      .filter { it.isNotBlank() } // Remove empty strings from split
      .mapIndexed { index, moveStr ->
         try {
            // Split "0,2->1,3" into ["0,2", "1,3"]
            val parts = moveStr.split("->")
            val from = formatCoords(parts[0])
            val to = formatCoords(parts[1])

            // Create object: "A3 ➝ B4"
            // Even index (0, 2...) is Host (White), Odd is Guest (Black)
            ParsedMove(index + 1, "$from ➝ $to", index % 2 == 0)
         } catch (e: Exception) {
            ParsedMove(index + 1, "Error", true)
         }
      }
}

// Converts coordinates "0,2" -> "A3" (Chess notation)
fun formatCoords(coordStr: String): String {
   val split = coordStr.split(",").map { it.toInt() }
   // X (0) + 65 = 'A', Y (2) + 1 = 3
   return "${(split[0] + 65).toChar()}${split[1] + 1}"
}
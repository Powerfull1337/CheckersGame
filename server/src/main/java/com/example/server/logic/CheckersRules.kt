package com.example.server.logic

import com.example.server.models.ActiveGame

object CheckersRules {

   // Checks if a specific piece can capture an enemy (multi-jump logic)
   fun hasMoreCaptures(board: Array<IntArray>, x: Int, y: Int, playerPiece: Int): Boolean {
      val enemyPiece = if (playerPiece == 1) 2 else 1
      val directions = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1) // 4 diagonals

      for ((dy, dx) in directions) {
         val destY = y + (dy * 2) // Target is 2 steps away
         val destX = x + (dx * 2)

         // Check boundaries
         if (destY in 0..7 && destX in 0..7) {
            // Condition: Enemy in the middle AND Empty landing spot
            if (board[y + dy][x + dx] == enemyPiece && board[destY][destX] == 0) return true
         }
      }
      return false
   }

   // Scans the whole board to see if the player has ANY legal moves (Loss condition check)
   fun hasValidMoves(board: Array<IntArray>, playerId: Int, game: ActiveGame): Boolean {
      val myPiece = if (playerId == game.player1Id) 1 else 2
      val moveDir = if (myPiece == 1) -1 else 1 // P1 moves UP (-1), P2 moves DOWN (+1)

      for (y in 0..7) {
         for (x in 0..7) {
            if (board[y][x] == myPiece) {

               // 1. Check simple forward moves
               val simpleMoves = listOf(x - 1, x + 1)
               for (nextX in simpleMoves) {
                  val nextY = y + moveDir
                  if (nextY in 0..7 && nextX in 0..7 && board[nextY][nextX] == 0) return true
               }

               // 2. Check captures (jumps) in any direction
               if (hasMoreCaptures(board, x, y, myPiece)) return true
            }
         }
      }
      return false
   }
}
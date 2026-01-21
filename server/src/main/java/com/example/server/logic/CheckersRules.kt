package com.example.server.logic

import com.example.server.models.ActiveGame

object CheckersRules {
   fun hasMoreCaptures(board: Array<IntArray>, x: Int, y: Int, playerPiece: Int): Boolean {
      val enemyPiece = if (playerPiece == 1) 2 else 1
      val directions = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
      for ((dy, dx) in directions) {
         val destY = y + (dy * 2)
         val destX = x + (dx * 2)
         if (destY in 0..7 && destX in 0..7) {
            if (board[y + dy][x + dx] == enemyPiece && board[destY][destX] == 0) return true
         }
      }
      return false
   }

   fun hasValidMoves(board: Array<IntArray>, playerId: Int, game: ActiveGame): Boolean {
      val myPiece = if (playerId == game.player1Id) 1 else 2


      val moveDir = if (myPiece == 1) -1 else 1

      for (y in 0..7) {
         for (x in 0..7) {
            if (board[y][x] == myPiece) {

               val simpleMoves = listOf(x - 1, x + 1)
               for (nextX in simpleMoves) {
                  val nextY = y + moveDir
                  if (nextY in 0..7 && nextX in 0..7 && board[nextY][nextX] == 0) return true
               }

               if (hasMoreCaptures(board, x, y, myPiece)) return true
            }
         }
      }
      return false
   }
}
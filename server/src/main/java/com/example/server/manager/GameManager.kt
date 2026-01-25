package com.example.server.manager

import com.example.server.database.Games
import com.example.server.database.Users
import com.example.server.logic.CheckersRules
import com.example.server.models.ActiveGame
import com.example.server.models.GameState
import com.example.server.models.MoveRequest
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

object GameManager {
   // Thread-safe map to hold games in memory (RAM)
   val activeGames = ConcurrentHashMap<Int, ActiveGame>()

   // Called on server start to load unfinished games from DB
   fun restoreActiveGames() {
      transaction {
         // Select games where there is no winner yet
         Games.select { Games.winnerName.isNull() }.forEach { row ->
            try {
               val gameId = row[Games.id]
               val hostId = row[Games.hostId]
               val guestId = row[Games.guestId]
               val turnId = row[Games.currentTurn] ?: hostId
               val rawBoard = row[Games.boardState]

               // Parse JSON board back to Array<IntArray>
               val board: Array<IntArray> = if (rawBoard.isNotBlank()) {
                  Json.decodeFromString<List<List<Int>>>(rawBoard).map { it.toIntArray() }.toTypedArray()
               } else {
                  // Fallback for empty state (standard setup)
                  Array(8) { y -> IntArray(8) { x -> if ((x + y) % 2 != 0 && (y < 3 || y > 4)) if (y < 3) 2 else 1 else 0 } }
               }

               val restoredGame = ActiveGame(gameId, hostId, guestId, board, turnId)

               // Give restored games 5 minutes to reconnect before cleanup
               restoredGame.cleanupDeadline = System.currentTimeMillis() + 300_000
               restoredGame.cleanupJob = CoroutineScope(Dispatchers.Default).launch {
                  delay(300_000)
                  if (restoredGame.sessions.isEmpty()) {
                     activeGames.remove(gameId)
                     println("Restored Game $gameId expired")
                  }
               }

               activeGames[gameId] = restoredGame
               println("Restored game #$gameId")
            } catch (e: Exception) { e.printStackTrace() }
         }
      }
   }

   // Sends the current board state to both connected players via WebSockets
   suspend fun broadcastGameState(g: ActiveGame) {
      val currentSessions = g.sessions.toMap()
      currentSessions.forEach { (uid, session) ->
         if (!session.isActive) return@forEach
         try {
            val opponentId = if (uid == g.player1Id) g.player2Id else g.player1Id
            val isOpponentOnline = if (opponentId != null) g.sessions.containsKey(opponentId) else false

            val state = GameState(
               g.board.map { it.toList() },
               g.currentTurnId,
               g.player1Id,
               g.player2Id,
               g.winnerId,
               g.rematchRequested.size,
               isOpponentConnected = isOpponentOnline
            )
            session.send(Json.encodeToString(state))
         } catch (e: Exception) {
            g.sessions.remove(uid) // Remove broken session
         }
      }
   }

   suspend fun processMove(g: ActiveGame, uid: Int, m: MoveRequest) {
      if (g.currentTurnId != uid) return // Not your turn
      val piece = g.board[m.fromY][m.fromX]
      val isHost = uid == g.player1Id
      val isValidOwner = (isHost && piece == 1) || (!isHost && piece == 2)
      if (!isValidOwner) return // Not your piece

      // Move piece in memory
      g.board[m.fromY][m.fromX] = 0
      g.board[m.toY][m.toX] = piece

      var turnSwitched = true

      // Check for Capture (Jump over 2 squares)
      if (abs(m.fromX - m.toX) == 2 && abs(m.fromY - m.toY) == 2) {
         val midX = (m.fromX + m.toX) / 2
         val midY = (m.fromY + m.toY) / 2
         g.board[midY][midX] = 0 // Remove captured piece

         // If more captures available, turn does NOT switch
         if (CheckersRules.hasMoreCaptures(g.board, m.toX, m.toY, piece)) turnSwitched = false
      }

      if (turnSwitched) {
         g.currentTurnId = if (g.currentTurnId == g.player1Id) g.player2Id!! else g.player1Id
      }

      // Check Win Condition (No valid moves left for next player)
      if (!CheckersRules.hasValidMoves(g.board, g.currentTurnId, g)) {
         g.winnerId = uid
         val wName = transaction { Users.select { Users.id eq uid }.single()[Users.name] }
         transaction { Games.update({ Games.id eq g.dbGameId }) { it[winnerName] = wName } }
      }

      // PERSISTENCE: Save state to DB asynchronously
      CoroutineScope(Dispatchers.IO).launch {
         try {
            transaction {
               // Append move to history string
               exec("UPDATE games SET history = history || '${m.fromX},${m.fromY}->${m.toX},${m.toY};' WHERE id = ${g.dbGameId}")

               // Save current board JSON and turn ID
               val boardJson = Json.encodeToString(g.board.map { it.toList() })
               Games.update({ Games.id eq g.dbGameId }) {
                  it[boardState] = boardJson
                  it[currentTurn] = g.currentTurnId
               }
            }
         } catch (e: Exception) { e.printStackTrace() }
      }

      broadcastGameState(g)
   }

   suspend fun processRematch(g: ActiveGame, uid: Int) {
      g.rematchRequested.add(uid)
      if (g.rematchRequested.size >= 2) {
         // Reset board
         val newBoard = Array(8) { y -> IntArray(8) { x -> if ((x + y) % 2 != 0 && (y < 3 || y > 4)) if (y < 3) 2 else 1 else 0 } }
         val newBoardJson = Json.encodeToString(newBoard.map { it.toList() })

         // Create new DB Entry (Swap Host/Guest)
         val newDbId = transaction {
            Games.insert {
               it[hostId] = g.player2Id!!
               it[guestId] = g.player1Id
               it[boardState] = newBoardJson
               it[currentTurn] = g.player2Id!!
            } get Games.id
         }

         // Update Memory Object
         g.dbGameId = newDbId
         val temp = g.player1Id
         g.player1Id = g.player2Id!!
         g.player2Id = temp
         g.board = newBoard
         g.winnerId = null
         g.currentTurnId = g.player1Id
         g.rematchRequested.clear()
      }
      broadcastGameState(g)
   }

   // Removes game from memory if no players connected for 1 mins
   fun scheduleCleanup(gameId: Int) {
      val g = activeGames[gameId] ?: return
      if (g.sessions.isEmpty()) {
         g.cleanupDeadline = System.currentTimeMillis() + 60_000 // 3 min
         g.cleanupJob = CoroutineScope(Dispatchers.Default).launch {
            delay(60_000)
            if (g.sessions.isEmpty()) {
               activeGames.remove(gameId)
            }
         }
      }
   }
}
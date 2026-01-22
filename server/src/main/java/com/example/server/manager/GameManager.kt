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
   val activeGames = ConcurrentHashMap<Int, ActiveGame>()
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

            g.sessions.remove(uid)
         }
      }
   }

   suspend fun processMove(g: ActiveGame, uid: Int, m: MoveRequest) {
      if (g.currentTurnId != uid) return

      val piece = g.board[m.fromY][m.fromX]
      val isHost = uid == g.player1Id
      val isValidOwner = (isHost && piece == 1) || (!isHost && piece == 2)
      if (!isValidOwner) return


      g.board[m.fromY][m.fromX] = 0
      g.board[m.toY][m.toX] = piece

      var turnSwitched = true

      if (abs(m.fromX - m.toX) == 2 && abs(m.fromY - m.toY) == 2) {
         val midX = (m.fromX + m.toX) / 2
         val midY = (m.fromY + m.toY) / 2
         g.board[midY][midX] = 0
         if (CheckersRules.hasMoreCaptures(g.board, m.toX, m.toY, piece)) turnSwitched = false
      }

      if (turnSwitched) {
         g.currentTurnId = if (g.currentTurnId == g.player1Id) g.player2Id!! else g.player1Id
      }

      if (!CheckersRules.hasValidMoves(g.board, g.currentTurnId, g)) {
         g.winnerId = uid
         val wName = transaction { Users.select { Users.id eq uid }.single()[Users.name] }
         transaction { Games.update({ Games.id eq g.dbGameId }) { it[winnerName] = wName } }
      }


      try {
         transaction {

            exec("UPDATE games SET history = history || '${m.fromX},${m.fromY}->${m.toX},${m.toY};' WHERE id = ${g.dbGameId}")
         }
      } catch (e: Exception) {}

      broadcastGameState(g)
   }


   suspend fun processRematch(g: ActiveGame, uid: Int) {
      g.rematchRequested.add(uid)
      if (g.rematchRequested.size >= 2) {

         val newDbId = transaction {
            Games.insert {
               it[hostId] = g.player2Id!!
               it[guestId] = g.player1Id
            } get Games.id
         }

         g.dbGameId = newDbId
         val temp = g.player1Id
         g.player1Id = g.player2Id!!
         g.player2Id = temp
         g.board = Array(8) { y -> IntArray(8) { x -> if ((x + y) % 2 != 0 && (y < 3 || y > 4)) if (y < 3) 2 else 1 else 0 } }
         g.winnerId = null
         g.currentTurnId = g.player1Id
         g.rematchRequested.clear()
      }
      broadcastGameState(g)
   }


   fun scheduleCleanup(gameId: Int) {
      val g = activeGames[gameId] ?: return
      if (g.sessions.isEmpty()) {

         g.cleanupDeadline = System.currentTimeMillis() + 180_000

         g.cleanupJob = CoroutineScope(Dispatchers.Default).launch {
            delay(180_000)
            if (g.sessions.isEmpty()) {
               activeGames.remove(gameId)
               println("Game $gameId destroyed")
            }
         }
      }
   }
}
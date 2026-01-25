package com.example.server.routes

import com.example.server.manager.GameManager
import com.example.server.database.Games
import com.example.server.database.Users
import com.example.server.models.*
import com.example.server.util.JwtConfig
import com.example.server.util.userId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.gameRoutes() {

   // --- Public Route (Login/Register) ---
   post("/auth") {
      try {
         val req = call.receive<AuthRequest>()
         val user = transaction {
            // Find existing user or create new one
            Users.select { (Users.name eq req.name) and (Users.pin eq req.pin) }.singleOrNull()
               ?: run {
                  val id = Users.insert { it[name] = req.name; it[pin] = req.pin } get Users.id
                  Users.select { Users.id eq id }.single()
               }
         }
         val uid = user[Users.id]
         val token = JwtConfig.generateToken(uid)
         call.respond(AuthResponse(token, uid, user[Users.name]))
      } catch (e: Exception) { call.respond(HttpStatusCode.InternalServerError) }
   }

   // --- Protected Routes (Require JWT) ---
   authenticate("auth-jwt", strategy = AuthenticationStrategy.Optional) {

      get("/lobby") {
         val uid = try { call.userId } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid userId/token")
            return@get
         }
         // Filter games: Only show open games or games where I am a player
         val list = GameManager.activeGames.values.filter {
            it.player2Id == null || (it.player1Id == uid || it.player2Id == uid)
         }.map { g ->
            val host = transaction {
               Users.select { Users.id eq g.player1Id }
                  .singleOrNull()?.get(Users.name) ?: "Unknown"
            }
            val isMine = (g.player1Id == uid || g.player2Id == uid)
            GameLobbyItem(g.dbGameId, host, isMine, g.sessions.size, g.cleanupDeadline)
         }
         call.respond(list)
      }

      post("/create") {
         val uid = try { call.userId } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, "Missing userId")
            return@post
         }

         val board = Array(8) { y -> IntArray(8) { x -> if ((x + y) % 2 != 0 && (y < 3 || y > 4)) if (y < 3) 2 else 1 else 0 } }
         val boardJson = Json.encodeToString(board.map { it.toList() })

         val gid = transaction {
            Games.insert {
               it[hostId] = uid
               it[boardState] = boardJson
               it[currentTurn] = uid
            } get Games.id
         }
         GameManager.activeGames[gid] = ActiveGame(gid, uid, null, board, uid)
         call.respond(mapOf("gameId" to gid))
      }

      get("/history") {
         val uid = call.userId
         val historyList = transaction {
            // Fetch last 20 games where user played (and didn't delete history)
            Games.select { ((Games.hostId eq uid) and (Games.deletedByHost eq false)) or ((Games.guestId eq uid) and (Games.deletedByGuest eq false)) }
               .orderBy(Games.date to SortOrder.DESC).limit(20)
               .mapNotNull { row ->
                  val opponentId = if (row[Games.hostId] == uid) row[Games.guestId] else row[Games.hostId]
                  if (opponentId == null) return@mapNotNull null
                  val opponentName = Users.select { Users.id eq opponentId }.single()[Users.name]
                  HistoryItem(row[Games.id], opponentName, row[Games.winnerName] ?: "In Progress", row[Games.history])
               }
         }
         call.respond(historyList)
      }

      delete("/history") {
         val uid = call.userId
         // Soft delete history
         transaction {
            Games.update({ Games.hostId eq uid }) { it[deletedByHost] = true }
            Games.update({ Games.guestId eq uid }) { it[deletedByGuest] = true }
         }
         call.respond(HttpStatusCode.OK)
      }

      // WebSocket Game Logic
      webSocket("/game/{id}") {
         val uid = try { call.userId } catch (e: Exception) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
            return@webSocket
         }

         val socketGid = call.parameters["id"]?.toInt() ?: return@webSocket
         val g = GameManager.activeGames[socketGid] ?: return@webSocket

         // Cancel cleanup since someone connected
         g.cleanupJob?.cancel()
         g.cleanupJob = null
         g.cleanupDeadline = null

         // Assign guest if slot empty
         if (g.player2Id == null && uid != g.player1Id) {
            g.player2Id = uid
            transaction { Games.update({ Games.id eq g.dbGameId }) { it[guestId] = uid } }
         }

         // Handle reconnection (close old socket)
         g.sessions[uid]?.close(CloseReason(CloseReason.Codes.NORMAL, "Reconnected"))
         g.sessions[uid] = this
         GameManager.broadcastGameState(g)

         try {
            for (frame in incoming) {
               if (frame is Frame.Text) {
                  val text = frame.readText()
                  if (text == "REMATCH") GameManager.processRematch(g, uid)
                  else if (g.winnerId == null) {
                     try {
                        val move = Json.decodeFromString<MoveRequest>(text)
                        GameManager.processMove(g, uid, move)
                     } catch (e: Exception) {}
                  }
               }
            }
         } finally {
            // Only remove session if it's the current one (handle race condition)
            if (g.sessions[uid] == this) {
               g.sessions.remove(uid)
               GameManager.broadcastGameState(g)
               GameManager.scheduleCleanup(socketGid)
            }
         }
      }
   }
}
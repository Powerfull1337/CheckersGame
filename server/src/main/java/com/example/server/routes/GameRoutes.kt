package com.example.server.routes

import com.example.server.manager.GameManager
import com.example.server.database.Games
import com.example.server.database.Users
import com.example.server.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.gameRoutes() {

   post("/auth") {
      try {
         val req = call.receive<AuthRequest>()
         val res = transaction {
            val u = Users.select { (Users.name eq req.name) and (Users.pin eq req.pin) }.singleOrNull()
            if (u != null) AuthResponse(u[Users.id], u[Users.name])
            else AuthResponse(Users.insert { it[name] = req.name; it[pin] = req.pin } get Users.id, req.name)
         }
         call.respond(res)
      } catch (e: Exception) { call.respond(HttpStatusCode.InternalServerError, "Auth error") }
   }

   get("/lobby") {
      try {
         val uid = call.request.queryParameters["userId"]?.toIntOrNull()
         val list = GameManager.activeGames.values.filter {
            it.player2Id == null || (uid != null && (it.player1Id == uid || it.player2Id == uid))
         }.map { g ->
            val host = transaction { Users.select { Users.id eq g.player1Id }.single()[Users.name] }
            val isMine = (uid != null && (g.player1Id == uid || g.player2Id == uid))
            GameLobbyItem(g.dbGameId, host, isMine)
         }
         call.respond(list)
      } catch (e: Exception) { call.respond(emptyList<GameLobbyItem>()) }
   }

   post("/create") {
      try {
         val uid = call.request.queryParameters["userId"]?.toInt() ?: return@post
         val gid = transaction { Games.insert { it[hostId] = uid } get Games.id }
         val board = Array(8) { y -> IntArray(8) { x -> if ((x + y) % 2 != 0 && (y < 3 || y > 4)) if (y < 3) 2 else 1 else 0 } }
         GameManager.activeGames[gid] = ActiveGame(gid, uid, null, board, uid)
         call.respond(mapOf("gameId" to gid))
      } catch (e: Exception) { call.respond(HttpStatusCode.InternalServerError) }
   }


   get("/history") {
      try {
         val uid = call.request.queryParameters["userId"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
         val historyList = transaction {

            Games.select {
               ((Games.hostId eq uid) and (Games.deletedByHost eq false)) or
                  ((Games.guestId eq uid) and (Games.deletedByGuest eq false))
            }
               .orderBy(Games.date to SortOrder.DESC)
               .limit(20)
               .mapNotNull { row ->
                  val opponentId = if (row[Games.hostId] == uid) row[Games.guestId] else row[Games.hostId]
                  if (opponentId == null) return@mapNotNull null

                  val opponentName = Users.select { Users.id eq opponentId }.single()[Users.name]
                  val winner = row[Games.winnerName] ?: "В процесі"
                  val moves = row[Games.history]

                  HistoryItem(row[Games.id], opponentName, winner, moves)
               }
         }
         call.respond(historyList)
      } catch (e: Exception) { call.respond(HttpStatusCode.InternalServerError) }
   }


   delete("/history") {
      try {
         val uid = call.request.queryParameters["userId"]?.toInt() ?: return@delete call.respond(HttpStatusCode.BadRequest)

         transaction {

            Games.update({ Games.hostId eq uid }) {
               it[deletedByHost] = true
            }

            Games.update({ Games.guestId eq uid }) {
               it[deletedByGuest] = true
            }
         }
         call.respond(HttpStatusCode.OK)
      } catch (e: Exception) {
         call.respond(HttpStatusCode.InternalServerError)
      }
   }

   webSocket("/game/{id}") {
      val socketGid = call.parameters["id"]?.toInt() ?: return@webSocket
      val uid = call.request.queryParameters["userId"]?.toInt() ?: return@webSocket
      val g = GameManager.activeGames[socketGid] ?: return@webSocket

      if (g.player2Id != null && uid != g.player1Id && uid != g.player2Id) {
         close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Game is full"))
         return@webSocket
      }

      g.cleanupJob?.cancel()
      g.cleanupJob = null

      if (g.player2Id == null && uid != g.player1Id) {
         g.player2Id = uid
         try { transaction { Games.update({ Games.id eq g.dbGameId }) { it[guestId] = uid } } } catch (e: Exception) {}
      }

      g.sessions[uid] = this
      GameManager.broadcastGameState(g)

      try {
         for (frame in incoming) {
            if (frame is Frame.Text) {
               val text = frame.readText()
               if (text == "REMATCH") {
                  GameManager.processRematch(g, uid)
               } else {
                  try {
                     if (g.winnerId == null) {
                        val move = Json.decodeFromString<MoveRequest>(text)
                        GameManager.processMove(g, uid, move)
                     }
                  } catch (e: Exception) {}
               }
            }
         }
      } finally {
         g.sessions.remove(uid)
         GameManager.broadcastGameState(g)
         GameManager.scheduleCleanup(socketGid)
      }
   }
}
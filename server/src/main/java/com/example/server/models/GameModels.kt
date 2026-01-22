package com.example.server.models

import io.ktor.websocket.*
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

@Serializable data class AuthRequest(val name: String, val pin: String)
@Serializable data class AuthResponse(val userId: Int, val name: String)
@Serializable
data class GameLobbyItem(
   val gameId: Int,
   val hostName: String,
   val isMyGame: Boolean = false,
   val playerCount: Int = 0,
   val destroyAt: Long? = null
)
@Serializable data class MoveRequest(val fromX: Int, val fromY: Int, val toX: Int, val toY: Int)
@Serializable data class HistoryItem(val gameId: Int, val opponentName: String, val winner: String, val moves: String)

@Serializable data class GameState(
   val board: List<List<Int>>,
   val turnPlayerId: Int,
   val player1Id: Int,
   val player2Id: Int?,
   val winnerId: Int? = null,
   val rematchRequests: Int = 0,
   val isOpponentConnected: Boolean = true
)

data class ActiveGame(
   var dbGameId: Int,
   var player1Id: Int,
   var player2Id: Int? = null,
   var board: Array<IntArray>,
   var currentTurnId: Int,
   var winnerId: Int? = null,
   val rematchRequested: MutableSet<Int> = mutableSetOf(),
   val sessions: ConcurrentHashMap<Int, WebSocketSession> = ConcurrentHashMap(),
   var cleanupJob: Job? = null,
   var cleanupDeadline: Long? = null
)
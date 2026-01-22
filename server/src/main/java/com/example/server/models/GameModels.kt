package com.example.server.models

import io.ktor.websocket.*
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

// Login data from client
@Serializable data class AuthRequest(val name: String, val pin: String)

// Auth success response with token
@Serializable data class AuthResponse(val token: String, val userId: Int, val name: String)

// Lobby list item
@Serializable data class GameLobbyItem(
   val gameId: Int,
   val hostName: String,
   val isMyGame: Boolean = false,
   val playerCount: Int = 0,
   val destroyAt: Long? = null // Deletion timestamp
)

// Move coordinates
@Serializable data class MoveRequest(val fromX: Int, val fromY: Int, val toX: Int, val toY: Int)

// Past game record
@Serializable data class HistoryItem(val gameId: Int, val opponentName: String, val winner: String, val moves: String)

// Full game state for UI
@Serializable data class GameState(
   val board: List<List<Int>>, // 8x8 Board
   val turnPlayerId: Int,      // Current turn
   val player1Id: Int,
   val player2Id: Int?,
   val winnerId: Int? = null,
   val rematchRequests: Int = 0,
   val isOpponentConnected: Boolean = true // Opponent online status
)

// In-memory game object (Server only)
data class ActiveGame(
   var dbGameId: Int,
   var player1Id: Int,
   var player2Id: Int? = null,
   var board: Array<IntArray>,
   var currentTurnId: Int,
   var winnerId: Int? = null,
   val rematchRequested: MutableSet<Int> = mutableSetOf(),
   val sessions: ConcurrentHashMap<Int, WebSocketSession> = ConcurrentHashMap(), // Active sockets
   var cleanupJob: Job? = null,      // Auto-delete timer
   var cleanupDeadline: Long? = null
)
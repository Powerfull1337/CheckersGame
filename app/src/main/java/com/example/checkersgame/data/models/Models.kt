package com.example.checkersgame.data.models

import kotlinx.serialization.Serializable

// Payload sent by client for login/registration
@Serializable data class AuthRequest(val name: String, val pin: String)

// Response from server containing the JWT Token
@Serializable data class AuthResponse(val token: String, val userId: Int, val name: String)

// Represents a single game row in the Lobby list
@Serializable data class GameLobbyItem(
   val gameId: Int,
   val hostName: String,
   val isMyGame: Boolean = false,
   val playerCount: Int = 0,
   val destroyAt: Long? = null // Timestamp when the empty game will be deleted
)

// Payload sent by client when making a move
@Serializable data class MoveRequest(val fromX: Int, val fromY: Int, val toX: Int, val toY: Int)

// Represents a past game in the History screen
@Serializable data class HistoryItem(val gameId: Int, val opponentName: String, val winner: String, val moves: String)

// The complete game state sent via WebSocket to update UI
@Serializable data class GameState(
   val board: List<List<Int>>, // 8x8 grid: 0=empty, 1=white, 2=black
   val turnPlayerId: Int,      // Whose turn it is
   val player1Id: Int,
   val player2Id: Int?,
   val winnerId: Int? = null,
   val rematchRequests: Int = 0,
   val isOpponentConnected: Boolean = true // Used to show "Opponent Offline" warning
)
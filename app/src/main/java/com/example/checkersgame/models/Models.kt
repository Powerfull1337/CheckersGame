package com.example.checkersgame.models

import kotlinx.serialization.Serializable

@Serializable data class AuthRequest(val name: String, val pin: String)
@Serializable data class AuthResponse(val userId: Int, val name: String)
@Serializable data class GameLobbyItem(val gameId: Int, val hostName: String, val isMyGame: Boolean = false)
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

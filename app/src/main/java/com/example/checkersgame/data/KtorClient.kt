package com.example.checkersgame.data

import com.example.checkersgame.presentation.core.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json

object KtorClient {
   // Singleton HTTP Client using CIO engine
   val client = HttpClient(CIO) {

      // Enable WebSockets with heartbeat
      install(WebSockets) {
         pingInterval = 10_000 // Keep connection alive (every 10s)
      }

      // JSON Serialization setup
      install(ContentNegotiation) { json() }

      // Automatic JWT Authentication
      install(Auth) {
         bearer {
            loadTokens {
               // Load saved token from local storage
               val token = TokenManager.getToken()
               if (token != null) BearerTokens(token, "") else null
            }
            // Token refresh logic (omitted for simplicity)
            refreshTokens { null }
         }
      }

      // Default headers applied to ALL requests
      defaultRequest {
         // Bypass Ngrok warning page for free tier
         header("ngrok-skip-browser-warning", "true")
      }
   }
}
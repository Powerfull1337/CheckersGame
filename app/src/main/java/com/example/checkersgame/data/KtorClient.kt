package com.example.checkersgame.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json

object KtorClient {
   val client = HttpClient(CIO) {
      install(WebSockets) {
         pingInterval = 10_000
      }
      install(ContentNegotiation) { json() }
      defaultRequest {
         header("ngrok-skip-browser-warning", "true")
      }
   }
}
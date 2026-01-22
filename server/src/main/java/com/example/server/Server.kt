package com.example.server

import com.example.server.database.DatabaseFactory
import com.example.server.manager.GameManager
import com.example.server.routes.gameRoutes
import com.example.server.util.JwtConfig
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.time.Duration

fun main() {
   // Init Database
   DatabaseFactory.init()

   // Restore previous game states from DB
   GameManager.restoreActiveGames()

   // Start Server
   embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
      install(ContentNegotiation) { json() }
      install(WebSockets) {
         pingPeriod = Duration.ofSeconds(30)
         timeout = Duration.ofSeconds(30)
         maxFrameSize = Long.MAX_VALUE
         masking = false
      }

      install(Authentication) {
         jwt("auth-jwt") {
            realm = "Checkers Game"
            verifier(JwtConfig.VERIFIER)
            validate { credential ->
               if (credential.payload.getClaim("id").asInt() != null) JWTPrincipal(credential.payload) else null
            }
         }
      }

      routing {
         gameRoutes()
      }
   }.start(wait = true)
}
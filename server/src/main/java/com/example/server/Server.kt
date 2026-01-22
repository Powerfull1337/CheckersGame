package com.example.server

import com.example.server.database.DatabaseFactory
import com.example.server.routes.gameRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.time.Duration



// ПОЯСНЕННЯ (Ngrok):
// Оскільки сервер запущено локально на ноутбуці (localhost:8080),
// реальний Android-телефон не може до нього підключитися напряму через USB/Wi-Fi без налаштування мережі.
// Тому використовується сервіс Ngrok(https://ngrok.com/docs/start) для тунелювання локального сервера в публічний інтернет.

fun main() {
   DatabaseFactory.init()

   embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
      install(ContentNegotiation) { json() }
      install(WebSockets) {
         pingPeriod = Duration.ofSeconds(30)
         timeout = Duration.ofSeconds(30)
         maxFrameSize = Long.MAX_VALUE
         masking = false
      }
      routing {
         gameRoutes()
      }
   }.start(wait = true)
}
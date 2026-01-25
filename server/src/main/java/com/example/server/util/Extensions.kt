package com.example.server.util

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

// Extension property to easily get UserID from the JWT Token
val ApplicationCall.userId: Int
   get() {
      val fromMiddleware = attributes.getOrNull(UserAttributeKey)
      if (fromMiddleware != null) return fromMiddleware

      return principal<JWTPrincipal>()
         ?.payload
         ?.getClaim("id")
         ?.asInt()
         ?: throw IllegalStateException("Token invalid or missing 'id' claim")
   }
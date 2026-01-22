package com.example.server.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import java.util.Date

object JwtConfig {
   // Load JWT secret from .env or use fallback
   private val dotenv = dotenv { ignoreIfMissing = true; directory = "./" }
   private val JWT_SECRET = dotenv["JWT_SECRET"] ?: System.getenv("JWT_SECRET")

   private const val ISSUER = "checkers-game"
   private const val VALIDITY_IN_MS = 36_000_000 // 10 hours

   private val algorithm = Algorithm.HMAC256(JWT_SECRET)

   val VERIFIER = JWT.require(algorithm)
      .withIssuer(ISSUER)
      .build()

   fun generateToken(userId: Int): String {
      return JWT.create()
         .withSubject("Authentication")
         .withIssuer(ISSUER)
         .withClaim("id", userId)
         .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_IN_MS))
         .sign(algorithm)
   }
}
package com.example.server.util

import io.ktor.server.application.*
import io.ktor.util.*

val UserAttributeKey = AttributeKey<Int>("UserId")

val UserIdMiddleware = createApplicationPlugin("UserIdMiddleware") {
   onCall { call ->
      call.request.queryParameters["userId"]?.toIntOrNull()?.let {
         call.attributes.put(UserAttributeKey, it)
      }
   }
}
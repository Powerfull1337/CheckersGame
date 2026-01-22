package com.example.server.database

import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction



object DatabaseFactory {
   fun init() {
      val dotenv = dotenv {
         ignoreIfMissing = true
         directory = "./"
      }
      val dbUrl = dotenv["DB_URL"] ?: System.getenv("DB_URL")
      val dbUser = dotenv["DB_USER"] ?: System.getenv("DB_USER")
      val dbPassword = dotenv["DB_PASSWORD"] ?: System.getenv("DB_PASSWORD")

      Database.connect(url = dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword)

      transaction {

         // SchemaUtils.drop(Games)
         // SchemaUtils.drop(Users)

         SchemaUtils.create(Users, Games)
      }
   }
}
object Users : Table() {
   val id = integer("id").autoIncrement()
   val name = varchar("name", 50)
   val pin = varchar("pin", 4)
   override val primaryKey = PrimaryKey(id)
}

object Games : Table() {
   val id = integer("id").autoIncrement()
   val hostId = integer("host_id")
   val guestId = integer("guest_id").nullable()
   val history = text("history").default("")
   val winnerName = varchar("winner_name", 50).nullable()
   val date = long("date").default(System.currentTimeMillis())

   val deletedByHost = bool("deleted_by_host").default(false)
   val deletedByGuest = bool("deleted_by_guest").default(false)

   override val primaryKey = PrimaryKey(id)
}


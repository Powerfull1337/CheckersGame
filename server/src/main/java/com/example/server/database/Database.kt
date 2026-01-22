package com.example.server.database

import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
   fun init() {
      // Load environment variables for DB credentials
      val dotenv = dotenv { ignoreIfMissing = true; directory = "./" }
      val dbUrl = dotenv["DB_URL"]
      val dbUser = dotenv["DB_USER"]
      val dbPassword = dotenv["DB_PASSWORD"]

      // Connect to PostgreSQL database
      Database.connect(url = dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword)

      transaction {
         // Uncomment lines below to reset database schema
         // SchemaUtils.drop(Games)
         // SchemaUtils.drop(Users)

         // Create tables if they do not exist
         SchemaUtils.create(Users, Games)
      }
   }
}

object Users : Table() {
   val id = integer("id").autoIncrement()
   val name = varchar("name", 50)
   val pin = varchar("pin", 4) // Simple PIN for authentication
   override val primaryKey = PrimaryKey(id)
}

object Games : Table() {
   val id = integer("id").autoIncrement()
   val hostId = integer("host_id")
   val guestId = integer("guest_id").nullable()
   val history = text("history").default("") // Stores moves string (e.g. "0,1->1,2;")
   val winnerName = varchar("winner_name", 50).nullable()
   val date = long("date").default(System.currentTimeMillis())

   // Fields for game state persistence (Restoration)
   val boardState = text("board_state").default("") // JSON string of the board array
   val currentTurn = integer("current_turn").nullable() // ID of the player who moves next

   // Soft delete flags for history visibility
   val deletedByHost = bool("deleted_by_host").default(false)
   val deletedByGuest = bool("deleted_by_guest").default(false)

   override val primaryKey = PrimaryKey(id)
}
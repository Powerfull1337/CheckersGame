plugins {
   alias(libs.plugins.jetbrains.kotlin.jvm)
   id("io.ktor.plugin") version "2.3.7"
   id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
   application
}

group = "com.example"
version = "0.0.1"

application {
   mainClass.set("com.example.server.ServerKt")
}



dependencies {
   implementation("io.ktor:ktor-server-core:2.3.7")
   implementation("io.ktor:ktor-server-netty:2.3.7")
   implementation("io.ktor:ktor-server-websockets:2.3.7")
   implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
   implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
   implementation("org.jetbrains.exposed:exposed-core:0.41.1")
   implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
   implementation("org.postgresql:postgresql:42.6.0")
   implementation("ch.qos.logback:logback-classic:1.4.14")
   implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
}
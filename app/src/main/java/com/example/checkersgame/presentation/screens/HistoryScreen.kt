package com.example.checkersgame.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkersgame.data.KtorClient.client
import com.example.checkersgame.presentation.core.Config
import com.example.checkersgame.data.models.HistoryItem
import com.example.checkersgame.presentation.components.HistoryItemCard
import com.example.checkersgame.ui.theme.BoardBrownDark
import com.example.checkersgame.ui.theme.PieceGuestColor
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(userId: Int, onBack: () -> Unit) {
   var history by remember { mutableStateOf(listOf<HistoryItem>()) }
   var showClearDialog by remember { mutableStateOf(false) }
   val scope = rememberCoroutineScope()
   val context = LocalContext.current

   // 1. Fetch history when screen opens
   LaunchedEffect(Unit) {
      try {
         // Token is automatically added by KtorClient
         val response = client.get("${Config.HOST_URL}/history")
         history = Json.decodeFromString(response.bodyAsText())
      } catch (e: Exception) {}
   }

   // 2. Clear History Dialog
   if (showClearDialog) {
      AlertDialog(
         onDismissRequest = { showClearDialog = false },
         title = { Text("Очистити історію?") },
         text = { Text("Це видалить всі записи про ігри.") },
         confirmButton = {
            Button(onClick = {
               scope.launch {
                  try {
                     // Send soft-delete request
                     client.delete("${Config.HOST_URL}/history")
                     history = emptyList()
                     Toast.makeText(context, "Очищено", Toast.LENGTH_SHORT).show()
                  } catch (e: Exception) {}
                  showClearDialog = false
               }
            }, colors = ButtonDefaults.buttonColors(containerColor = PieceGuestColor)) { Text("Видалити") }
         },
         dismissButton = { Button(onClick = { showClearDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Ні") } }
      )
   }

   Scaffold(
      topBar = {
         TopAppBar(
            title = { Text("Архів партій", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
            // Show delete button only if history exists
            actions = { if (history.isNotEmpty()) IconButton(onClick = { showClearDialog = true }) { Icon(Icons.Default.Delete, null, tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BoardBrownDark, titleContentColor = Color.White)
         )
      }
   ) { padding ->
      if (history.isEmpty()) {
         Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Історія порожня", color = Color.Gray) }
      } else {
         LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(history) { item -> HistoryItemCard(item, userId) }
         }
      }
   }
}
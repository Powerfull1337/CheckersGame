package com.example.checkersgame.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checkersgame.Config
import com.example.checkersgame.client
import com.example.checkersgame.models.AuthRequest
import com.example.checkersgame.models.AuthResponse
import com.example.checkersgame.ui.theme.BoardBrownDark
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun LoginScreen(onSuccess: (Int) -> Unit) {
   var name by remember { mutableStateOf("") }
   var pin by remember { mutableStateOf("") }
   var isLoading by remember { mutableStateOf(false) }
   val scope = rememberCoroutineScope()
   val context = LocalContext.current

   Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Card(
         modifier = Modifier.fillMaxWidth(0.85f).shadow(12.dp, RoundedCornerShape(24.dp)),
         colors = CardDefaults.cardColors(containerColor = Color.White)
      ) {
         Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AccountCircle, null, Modifier.size(80.dp), tint = BoardBrownDark)
            Spacer(Modifier.height(16.dp))
            Text("Checkers Online", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = BoardBrownDark)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Нікнейм") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = pin, onValueChange = { if (it.length <= 4) pin = it }, label = { Text("PIN-код") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(32.dp))
            if (isLoading) CircularProgressIndicator(color = BoardBrownDark)
            else Button(
               onClick = {
                  isLoading = true
                  scope.launch {
                     try {
                        val response = client.post("${Config.HOST_URL}/auth") {
                           contentType(ContentType.Application.Json)
                           setBody(AuthRequest(name, pin))
                        }
                        onSuccess(Json.decodeFromString<AuthResponse>(response.bodyAsText()).userId)
                     } catch (e: Exception) { Toast.makeText(context, "Error", Toast.LENGTH_LONG).show() } finally { isLoading = false }
                  }
               },
               modifier = Modifier.fillMaxWidth().height(56.dp),
               shape = RoundedCornerShape(12.dp),
               colors = ButtonDefaults.buttonColors(containerColor = BoardBrownDark)
            ) { Text("Почати гру", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
         }
      }
   }
}
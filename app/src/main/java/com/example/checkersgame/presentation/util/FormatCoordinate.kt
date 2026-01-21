package com.example.checkersgame.presentation.util

fun formatCoordinate(x: Int, y: Int): String {
   val letter = (x + 65).toChar()
   val number = y + 1
   return "$letter$number"
}
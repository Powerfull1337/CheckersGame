package com.example.checkersgame.presentation.core

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object TokenManager {
   private const val PREFS_NAME = "auth_prefs"
   private const val KEY_TOKEN = "jwt_token"
   private const val KEY_USER_ID = "user_id"

   // Shared Preferences instance
   private lateinit var prefs: SharedPreferences

   // Must be called in MainActivity.onCreate()
   fun init(context: Context) {
      prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
   }

   // Saves the JWT token and User ID after successful login
   fun saveAuth(token: String, userId: Int) = prefs.edit {
      putString(KEY_TOKEN, token)
      putInt(KEY_USER_ID, userId)
   }

   // Retrieves the stored JWT token (or null if not logged in)
   fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

   // Retrieves the stored User ID (or -1 if not found)
   fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

   // Clears data on logout
   fun clear() = prefs.edit { clear() }
}
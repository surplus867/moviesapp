package com.minyu.moviesapp.core.auth

import android.content.Context
import androidx.core.content.edit

object SessionManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_USER_NAME = "user_name"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isLoggedIn(context: Context): Boolean = prefs(context).getBoolean(KEY_LOGGED_IN, false)

    fun setLoggedIn(context: Context, loggedIn: Boolean, userName: String = "") {
        prefs(context).edit {
            putBoolean(KEY_LOGGED_IN, loggedIn)
            putString(KEY_USER_NAME, userName)
        }
    }

    fun getUserName(context: Context): String? = prefs(context).getString(KEY_USER_NAME, null)

    fun logout(context: Context) {
        prefs(context).edit {
            putBoolean(KEY_LOGGED_IN, false)
            remove(KEY_USER_NAME)
        }
    }
}

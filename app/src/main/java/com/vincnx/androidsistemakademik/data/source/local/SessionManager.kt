package com.vincnx.androidsistemakademik.data.source.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        const val PREF_NAME = "SistemAkademikSession"
        const val IS_LOGIN = "IsLoggedIn"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_USER_ID = "user_id"
    }

    fun createLoginSession(email: String, role: String, userId: String) {
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_ROLE, role)
        editor.putString(KEY_USER_ID, userId)
        editor.commit()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGIN, false)
    }

    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user[KEY_EMAIL] = prefs.getString(KEY_EMAIL, null)
        user[KEY_ROLE] = prefs.getString(KEY_ROLE, null)
        user[KEY_USER_ID] = prefs.getString(KEY_USER_ID, null)
        return user
    }

    fun logoutUser() {
        editor.clear()
        editor.commit()
    }
} 
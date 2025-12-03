package com.example.list_wise

import android.content.Context
import androidx.core.content.edit

object UserStorage {

    private const val PREFS_NAME = "users_prefs"
    private const val KEY_USERS = "users_set"
    private const val SEPARATOR = "|||"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Grava um novo usuário (login = e-mail) */
    fun addUser(context: Context, user: User) {
        val p = prefs(context)
        val current = p.getStringSet(KEY_USERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add("${user.login}$SEPARATOR${user.password}")
        p.edit {
            putStringSet(KEY_USERS, current)
        }
    }

    /** Verifica se já existe alguém com esse login (e-mail) */
    fun userExists(context: Context, login: String): Boolean {
        val set = prefs(context).getStringSet(KEY_USERS, emptySet()) ?: emptySet()
        return set.any { it.substringBefore(SEPARATOR) == login }
    }

    /** Confere se login + senha batem com algum usuário salvo */
    fun validateLogin(context: Context, login: String, password: String): Boolean {
        val set = prefs(context).getStringSet(KEY_USERS, emptySet()) ?: emptySet()
        return set.any {
            val parts = it.split(SEPARATOR)
            parts.size == 2 && parts[0] == login && parts[1] == password
        }
    }

    /** Garante que sempre exista o usuário admin / admin */
    fun ensureAdmin(context: Context) {
        if (!userExists(context, "admin")) {
            addUser(context, User("admin", "admin"))
        }
    }
}

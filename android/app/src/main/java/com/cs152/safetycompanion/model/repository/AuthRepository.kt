package com.cs152.safetycompanion.model.repository

import com.cs152.safetycompanion.model.local.SessionStore
import com.cs152.safetycompanion.model.remote.ApiService
import com.cs152.safetycompanion.model.remote.AuthResponse
import com.cs152.safetycompanion.model.remote.LoginRequest
import com.cs152.safetycompanion.model.remote.RegisterRequest

/** MODEL (Family side): family sign-up, login, logout. */
class AuthRepository(
    private val api: ApiService,
    private val session: SessionStore,
) {
    val isLoggedIn: Boolean get() = session.familyToken != null
    val familyName: String? get() = session.familyName

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        save(api.login(LoginRequest(email.trim(), password)))
    }

    suspend fun register(name: String, email: String, password: String): Result<Unit> = runCatching {
        save(api.register(RegisterRequest(name.trim(), email.trim(), password)))
    }

    fun logout() {
        session.familyToken = null
        session.familyName = null
    }

    private fun save(response: AuthResponse) {
        session.familyToken = response.token
        session.familyName = response.name
    }
}

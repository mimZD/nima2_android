package org.eshragh.nima2.data.repository

import com.google.gson.JsonElement
import org.eshragh.nima2.data.pref.UserPreferencesRepository
import org.eshragh.nima2.data.remote.RetrofitClient
import org.eshragh.nima2.data.remote.model.LoginRequest

class AuthRepository(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend fun login(serverUrl: String, emailOrUsername: String, password: String): Result<String> {
        return try {
            val formattedUrl = serverUrl.trim().let { if (it.startsWith("http")) it else "https://$it" }
            val api = RetrofitClient.getApi(formattedUrl)
            val response = api.login(LoginRequest(emailOrUsername, password))

            if (response.isSuccessful) {
                val tokenElement = response.body()?.item
                val token = parseToken(tokenElement)
                if (!token.isNullOrBlank()) {
                    userPreferencesRepository.saveAuthData(formattedUrl, token)
                    Result.success(token)
                } else {
                    Result.failure(Exception("توکن معتبر از سرور دریافت نشد"))
                }
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "نام کاربری یا کلمه عبور اشتباه است"
                    404 -> "سرور یافت نشد. آدرس را بررسی کنید"
                    else -> "خطا در برقراری ارتباط با سرور (${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در شبکه: ${e.localizedMessage}"))
        }
    }

    private fun parseToken(element: JsonElement?): String? {
        if (element == null || element.isJsonNull) return null
        if (element.isJsonPrimitive) return element.asString
        if (element.isJsonObject) {
            val obj = element.asJsonObject
            if (obj.has("id")) return obj.get("id").asString
            if (obj.has("token")) return obj.get("token").asString
            if (obj.has("accessToken")) return obj.get("accessToken").asString
        }
        return null
    }

    suspend fun logout() {
        userPreferencesRepository.clearAuth()
    }
}

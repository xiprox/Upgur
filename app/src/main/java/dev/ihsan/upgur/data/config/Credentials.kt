package dev.ihsan.upgur.data.config

import android.content.Context
import android.content.SharedPreferences
import dev.ihsan.upgur.data.model.AuthResult
import dev.ihsan.upgur.util.ext.unitify
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A SharedPreferences manager object that takes care of storing user credentials.
 */
@Singleton
class Credentials @Inject constructor(appContext: Context) {
    private val prefs: SharedPreferences

    init {
        prefs = appContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    fun loadFrom(authResult: AuthResult) {
        accessToken = authResult.accessToken
        refreshToken = authResult.refreshToken
        tokenType = authResult.tokenType
        expiresIn = authResult.expiresIn
        username = authResult.accountUsername
        accountId = authResult.accountId
    }

    var username: String?
        set(value) = prefs.edit().putString(KEY_ACCOUNT_USERNAME, value).commit().unitify()
        get() = prefs.getString(KEY_ACCOUNT_USERNAME, null)

    var avatarUrl: String?
        set(value) = prefs.edit().putString(KEY_ACCOUNT_AVATAR_URL, value).commit().unitify()
        get() = prefs.getString(KEY_ACCOUNT_AVATAR_URL, null)

    var accountId: Int?
        set(value) = prefs.edit().putInt(KEY_ACCOUNT_ID, value ?: -1).commit().unitify()
        get() = prefs.getInt(KEY_ACCOUNT_ID, -1)

    var accessToken: String?
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).commit().unitify()
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)

    var refreshToken: String?
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).commit().unitify()
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)

    var tokenType: String?
        set(value) = prefs.edit().putString(KEY_TOKEN_TYPE, value).commit().unitify()
        get() = prefs.getString(KEY_TOKEN_TYPE, null)

    var expiresIn: Int?
        set(value) = prefs.edit().putInt(KEY_EXPIRES_IN, value ?: -1).commit().unitify()
        get() = prefs.getInt(KEY_EXPIRES_IN, 0)

    var didSecondStageAuth: Boolean
        set(value) = prefs.edit().putBoolean(KEY_DID_SECOND_STAGE_AUTH, value).commit().unitify()
        get() = prefs.getBoolean(KEY_DID_SECOND_STAGE_AUTH, false)


    val isAuthenticated: Boolean
        get() = username != null && accessToken != null && didSecondStageAuth

    fun logout() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val NAME = "credentials_storage"
        private const val KEY_ACCOUNT_USERNAME = "account_username"
        private const val KEY_ACCOUNT_AVATAR_URL = "account_avatar_url"
        private const val KEY_ACCOUNT_ID = "account_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_EXPIRES_IN = "expires_in"
        private const val KEY_DID_SECOND_STAGE_AUTH = "did_second_stage_auth"
    }
}
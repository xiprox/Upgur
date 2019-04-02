package dev.ihsan.upgur.data.model

import com.google.gson.annotations.SerializedName

class AuthResult {
    @SerializedName("access_token")
    var accessToken: String? = null
    @SerializedName("refresh_token")
    var refreshToken: String? = null
    @SerializedName("expires_in")
    val expiresIn: Int? = null
    @SerializedName("token_type")
    val tokenType: String? = null
    @SerializedName("account_username")
    val accountUsername: String? = null
    @SerializedName("account_id")
    val accountId: Int? = null
}
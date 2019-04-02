package dev.ihsan.upgur.data.remote

import com.google.gson.Gson
import dev.ihsan.upgur.data.config.Config
import dev.ihsan.upgur.data.config.Config.IMGUR_API_BASE_URL
import dev.ihsan.upgur.data.config.Config.IMGUR_OAUTH2_API_BASE_URL
import dev.ihsan.upgur.data.config.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImgurClient @Inject constructor() {

    @Inject
    lateinit var credentials: Credentials

    private val loggingInterceptor = HttpLoggingInterceptor().also {
        it.level = HttpLoggingInterceptor.Level.BODY
    }

    private val loggingClient =
        OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(IMGUR_API_BASE_URL)
        .client(loggingClient)
        .addConverterFactory(ImgurResponseConverterFactory(Gson()))
        .addCallAdapterFactory(LiveDataCallAdapterFactory())
        .build()
    private val service = retrofit.create(ImgurService::class.java)

    private val oauthRetrofit = Retrofit.Builder()
        .baseUrl(IMGUR_OAUTH2_API_BASE_URL)
        .client(loggingClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(LiveDataCallAdapterFactory())
        .build()
    private val oauthService = oauthRetrofit.create(ImgurOauthService::class.java)

    private val authHeaderValueWithAccessToken by lazy { "Bearer ${credentials.accessToken}" }
    private val authHeaderValueWithClientId by lazy { "Client-ID ${Config.IMGUR_OAUTH2_CLIENT_ID}" }

    fun oauth2Token(refreshToken: String?) = oauthService.oauth2Token(
        refreshToken = refreshToken ?: "",
        clientId = Config.IMGUR_OAUTH2_CLIENT_ID,
        clientSecret = Config.IMGUR_OAUTH2_CLIENT_SECRET,
        grantType = "refresh_token"
    )

    fun accountAvatar() = service.accountAvatar(authHeaderValueWithAccessToken, credentials.username)

    fun albums() = service.albums(authHeaderValueWithAccessToken, credentials.username)

    fun albumImages(albumId: String) = service.albumImages(authHeaderValueWithClientId, albumId)
}
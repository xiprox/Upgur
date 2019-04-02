package dev.ihsan.upgur.ui.login

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import dev.ihsan.upgur.Injector
import dev.ihsan.upgur.data.config.Credentials
import dev.ihsan.upgur.data.model.AuthResult
import dev.ihsan.upgur.data.remote.*
import javax.inject.Inject

class LoginAuthResponseViewModel : ViewModel() {
    @Inject
    lateinit var client: ImgurClient
    @Inject
    lateinit var credentials: Credentials

    val authResult = MediatorLiveData<Resource<AuthResult>>().also { Resource.loading(null) }

    fun doAuth() {
        Injector.get().inject(this)
        val response = client.oauth2Token(credentials.refreshToken)
        authResult.addSource(response) {
            when (it) {
                is ApiSuccessResponse<*> -> {
                    authResult.value = Resource.success((it as ApiSuccessResponse).body)
                }
                is ApiEmptyResponse<*> -> {
                    authResult.value = Resource.empty()
                }
                is ApiErrorResponse<*> -> {
                    authResult.value = Resource.error(
                        (it as ApiErrorResponse).errorMessage,
                        null
                    )
                }
            }
        }
    }
}
package dev.ihsan.upgur.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dev.ihsan.upgur.Injector
import dev.ihsan.upgur.MainActivity
import dev.ihsan.upgur.R
import dev.ihsan.upgur.data.config.Config
import dev.ihsan.upgur.data.config.Credentials
import dev.ihsan.upgur.data.model.AuthResult
import dev.ihsan.upgur.data.remote.Resource
import kotlinx.android.synthetic.main.activity_login_auth_response.*
import javax.inject.Inject

class LoginAuthResponseActivity : AppCompatActivity() {

    @Inject
    lateinit var credentials: Credentials

    private fun persistNewCredentials(result: AuthResult) {
        credentials.loadFrom(result)
        credentials.didSecondStageAuth = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_auth_response)
        Injector.get().inject(this)
        val model = ViewModelProviders.of(this).get(LoginAuthResponseViewModel::class.java)

        model.authResult.observe(this, Observer {
            var status = it?.status ?: Resource.Status.ERROR
            if (it?.data == null) {
                status = Resource.Status.ERROR
            }
            when (status) {
                Resource.Status.LOADING -> {
                    lceeView.showLoading()
                }
                Resource.Status.EMPTY, Resource.Status.ERROR -> {
                    lceeView.showError()
                }
                Resource.Status.SUCCESS -> {
                    persistNewCredentials(it.data!!)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        })

        intent.data?.let {
            if (it.scheme == Config.IMGUR_OAUTH2_REDIRECT_SCHEMA) {
                val error = it.getQueryParameter("error")
                if (error == null) {
                    var uriString = it.toString()
                    if (uriString.contains("#")) {
                        // Imgur url parameters start with a hash instead of a question mark when auth
                        // is successful. [Uri.getQueryParameter] does not work in that case and we'd
                        // rather it does.
                        uriString = uriString.replaceFirst("#", "?")
                    }
                    val uri = Uri.parse(uriString)
                    val accessToken = uri.getQueryParameter("access_token")
                    val expiresIn = uri.getQueryParameter("expires_in")
                    val tokenType = uri.getQueryParameter("token_type")
                    val refreshToken = uri.getQueryParameter("refresh_token")
                    val accountUsername = uri.getQueryParameter("account_username")
                    val accountId = uri.getQueryParameter("account_id")
                    credentials.let { c ->
                        c.accessToken = accessToken
                        c.expiresIn = expiresIn?.toIntOrNull() ?: -1
                        c.tokenType = tokenType
                        c.refreshToken = refreshToken
                        c.username = accountUsername
                        c.accountId = accountId?.toIntOrNull() ?: -1
                    }
                    model.doAuth()
                }
            }
        }
    }
}

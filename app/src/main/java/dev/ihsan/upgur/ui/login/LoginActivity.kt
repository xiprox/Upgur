package dev.ihsan.upgur.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import dev.ihsan.upgur.R
import dev.ihsan.upgur.data.config.Config
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.HttpUrl

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signInButton.setOnClickListener {
            val url = HttpUrl.parse(Config.IMGUR_OAUTH2_AUTH_URL)
                ?.newBuilder()
                ?.addQueryParameter("client_id", Config.IMGUR_OAUTH2_CLIENT_ID)
                ?.addQueryParameter("client_secret", Config.IMGUR_OAUTH2_CLIENT_SECRET)
                ?.addQueryParameter("response_type", Config.IMGUR_OAUTH2_RESPONSE_TYPE)
                ?.build()
            if (url != null) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url.url().toString())
                startActivity(i)
            }
        }
    }
}

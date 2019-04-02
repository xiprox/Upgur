package dev.ihsan.upgur

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.okhttp.OkHttpStack
import tr.xip.errorview.BuildConfig

class App : Application() {
    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        sInstance = this
        Fresco.initialize(this)
        component = DaggerAppComponent.builder()
            .contextModule(ContextModule(this))
            .build()
        super.onCreate()
        setUpUploadService()
    }

    private fun setUpUploadService() {
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID
        UploadService.HTTP_STACK = OkHttpStack()
        UploadService.PROGRESS_REPORT_INTERVAL = 500
    }

    companion object {
        private lateinit var sInstance: App

        @JvmStatic
        fun get(): App {
            return sInstance
        }
    }
}
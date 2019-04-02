package dev.ihsan.upgur

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.ihsan.upgur.data.config.Credentials
import dev.ihsan.upgur.data.db.ImageDao
import dev.ihsan.upgur.data.remote.upload.ImageUploader
import dev.ihsan.upgur.ui.login.LoginActivity
import net.gotev.uploadservice.UploadService

import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var credentials: Credentials

    @Inject
    lateinit var imageDao: ImageDao

    @Inject
    lateinit var executors: AppExecutors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Injector.get().inject(this)

        if (!credentials.isAuthenticated) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        enqueuePendingUploadSync()
        super.onResume()
    }

    override fun onPause() {
        enqueuePendingUploadSync()
        super.onPause()
    }

    /**
     * If we have pending uploads and we are not uploading them right now, schedule upload work.
     */
    private fun enqueuePendingUploadSync() {
        executors.diskIO().execute {
            val pendingUploads = imageDao.findAllPendingUploads()
            if (pendingUploads.isNotEmpty()) {
                if (UploadService.getTaskList().isEmpty()) {
                    ImageUploader.enqueue()
                }
            }
        }
    }
}

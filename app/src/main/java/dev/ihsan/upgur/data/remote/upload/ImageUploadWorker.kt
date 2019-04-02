package dev.ihsan.upgur.data.remote.upload

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import dev.ihsan.upgur.Injector
import dev.ihsan.upgur.data.config.Config
import dev.ihsan.upgur.data.config.Credentials
import dev.ihsan.upgur.data.db.ImageDao
import net.gotev.uploadservice.MultipartUploadRequest
import net.gotev.uploadservice.UploadNotificationConfig
import javax.inject.Inject

class ImageUploadWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @Inject
    lateinit var imageDao: ImageDao

    @Inject
    lateinit var credentials: Credentials

    @Inject
    lateinit var delegate: UploadStatusDelegate

    override fun doWork(): Result {
        Injector.get().inject(this)

        // Get a list of pending uploads
        val pendingUploads = imageDao.findAllPendingUploads()
        if (pendingUploads.isEmpty()) {
            // Nothing to upload? Great.
            return Result.success()
        }

        // Map album IDs to file urls
        val mapOfAlbumIdsToUrls = mutableMapOf<String, MutableList<String>>()
        pendingUploads.forEach {
            if (mapOfAlbumIdsToUrls[it.albumId ?: ""] == null) {
                mapOfAlbumIdsToUrls[it.albumId ?: ""] = mutableListOf()
            }
            mapOfAlbumIdsToUrls[it.albumId ?: ""]!!.add(it.id)
        }
        mapOfAlbumIdsToUrls.forEach {
            // Upload files for each album
            upload(albumId = it.key, urls = it.value, delegate = delegate)
        }

        // We can't know for sure if we succeeded with the async calls earlier. Returning retry will run this
        // work again, but if we had succeeded, list of pending uploads will be empty, and the work will return
        // success.
        return Result.retry()
    }

    private fun upload(albumId: String, urls: List<String>, delegate: UploadStatusDelegate) {
        val pathsAndUploadIds = mutableMapOf<String, String>()
        try {
            urls.forEach {
                val request = MultipartUploadRequest(context, it, Config.IMGUR_API_IMAGE_UPLOAD_URL)
                    .addFileToUpload(it.replaceFirst("file://", ""), "image")
                    .addHeader("Authorization", "Bearer ${credentials.accessToken}")
                    .addParameter("album", albumId)
                    .setNotificationConfig(UploadNotificationConfig())
                    .setDelegate(delegate)
                    .setMaxRetries(2)
                val uploadId = request.startUpload()
                pathsAndUploadIds[it] = uploadId
            }
        } catch (exc: Exception) {
            Log.e("UploadService", exc.message, exc)
        }
    }

    companion object {
        const val TAG = "upload-images"
    }
}
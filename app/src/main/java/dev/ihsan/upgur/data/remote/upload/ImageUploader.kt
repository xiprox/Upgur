package dev.ihsan.upgur.data.remote.upload

import androidx.work.*
import java.util.concurrent.TimeUnit

object ImageUploader {

    /**
     * Enqueues a Worker to sync pending uploads in local database with Imgur.
     *
     * It will run as soon as it finds an unmetered internet connection, and even if the app is completely killed.
     */
    fun enqueue() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()

        val request: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<ImageUploadWorker>()
                .setConstraints(constraints)
                .addTag(ImageUploadWorker.TAG)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

        WorkManager.getInstance()
            .beginUniqueWork("upload-images-work", ExistingWorkPolicy.REPLACE, request)
            .enqueue()
    }
}
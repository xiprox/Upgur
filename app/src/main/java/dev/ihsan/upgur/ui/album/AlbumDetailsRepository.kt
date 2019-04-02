package dev.ihsan.upgur.ui.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import dev.ihsan.upgur.AppExecutors
import dev.ihsan.upgur.data.db.AlbumDao
import dev.ihsan.upgur.data.db.ImageDao
import dev.ihsan.upgur.data.model.Album
import dev.ihsan.upgur.data.model.AlbumImage
import dev.ihsan.upgur.data.remote.ImgurClient
import dev.ihsan.upgur.data.remote.NetworkBoundResource
import dev.ihsan.upgur.data.remote.Resource
import dev.ihsan.upgur.util.RateLimiter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumDetailsRepository @Inject constructor(
    private val executors: AppExecutors,
    private val albumDao: AlbumDao,
    private val imageDao: ImageDao,
    private val client: ImgurClient
) {
    private val rateLimiter = RateLimiter<String>(30, TimeUnit.SECONDS)

    private fun rateLimitKey(albumId: String) = "album_$albumId"

    fun loadAlbum(id: String): LiveData<Resource<Album>>? {
        return Transformations.map(albumDao.findById(id)) {
            if (it != null) {
                Resource.success<Album>(it)
            } else {
                Resource.error<Album>("No album exists in DB with given ID", it)
            }
        }
    }

    fun loadImages(id: String): LiveData<Resource<List<AlbumImage>>> {
        return object : NetworkBoundResource<List<AlbumImage>, List<AlbumImage>>(executors) {
            override fun saveCallResult(result: List<AlbumImage>) {
                result.forEach { it.albumId = id }
                imageDao.insertAll(result)
            }

            override fun shouldFetch(data: List<AlbumImage>?): Boolean {
                return data == null || data.isEmpty() || rateLimiter.shouldFetch(rateLimitKey(id))
            }

            override fun loadFromDb() = imageDao.findAllByAlbumId(id)


            override fun createCall() = client.albumImages(id)

            override fun onFetchFailed() {
                rateLimiter.reset(rateLimitKey(id))
            }
        }.asLiveData()
    }

    fun addPendingUploads(albumId: String, urls: List<String>, onAdded: (() -> Unit)?) {
        val newImages: List<AlbumImage> = urls.map {
            AlbumImage(
                id = it,
                datetime = (System.currentTimeMillis() / 1000).toInt(),
                link = it,
                albumId = albumId,
                queuedForUpload = true
            )
        }
        executors.diskIO().execute {
            imageDao.insertAll(newImages)
            if (onAdded != null) onAdded()
        }
        rateLimiter.reset(rateLimitKey(albumId))
    }

    fun updateUploadProgress(imageKey: String?, uploadedBytes: Int, totalBytes: Int) {
        executors.diskIO().execute {
            val temporaryImage = getImageInUploadFromDb(imageKey) ?: return@execute
            temporaryImage.uploadedPercentage = uploadedBytes / totalBytes * 100
            imageDao.update(temporaryImage)
        }
    }

    fun updateUploadError(imageKey: String?, errorMessage: String) {
        executors.diskIO().execute {
            val temporaryImage = getImageInUploadFromDb(imageKey) ?: return@execute
            temporaryImage.uploadError = errorMessage
            imageDao.update(temporaryImage)
        }
    }

    fun updateUploadSuccess(imageKey: String?, responseImage: AlbumImage) {
        executors.diskIO().execute {
            val temporaryImage = getImageInUploadFromDb(imageKey) ?: return@execute
            responseImage.albumId = temporaryImage.albumId
            imageDao.delete(temporaryImage)
            imageDao.insert(responseImage)
        }
    }

    fun updateUploadCanceled(imageKey: String?) {
        executors.diskIO().execute {
            val temporaryImage = getImageInUploadFromDb(imageKey) ?: return@execute
            imageDao.delete(temporaryImage)
        }
    }

    private fun getImageInUploadFromDb(key: String?) = imageDao.findFirstByKeyOnce(key ?: "")

    fun resetRatelimit(albumId: String) {
        rateLimiter.reset(rateLimitKey(albumId))
    }
}
package dev.ihsan.upgur.data.remote.upload

import android.content.Context
import com.google.gson.Gson
import dev.ihsan.upgur.data.model.AlbumImage
import dev.ihsan.upgur.data.model.ImgurResponse
import dev.ihsan.upgur.ui.album.AlbumDetailsRepository
import dev.ihsan.upgur.util.ext.fromJson
import net.gotev.uploadservice.ServerResponse
import net.gotev.uploadservice.UploadInfo
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadStatusDelegate @Inject constructor() : net.gotev.uploadservice.UploadStatusDelegate {

    @Inject
    lateinit var albumDetailsRepository: AlbumDetailsRepository

    override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {
        albumDetailsRepository.updateUploadProgress(
            imageKey = uploadInfo?.uploadId,
            uploadedBytes = uploadInfo?.uploadedBytes?.toInt() ?: 0,
            totalBytes = uploadInfo?.totalBytes?.toInt() ?: 1
        )
    }

    override fun onError(
        context: Context?,
        uploadInfo: UploadInfo?,
        serverResponse: ServerResponse?,
        exception: Exception?
    ) {
        albumDetailsRepository.updateUploadError(
            imageKey = uploadInfo?.uploadId,
            errorMessage = exception?.toString() ?: serverResponse?.httpCode?.toString() ?: "Unknown error"
        )
    }

    override fun onCompleted(context: Context?, uploadInfo: UploadInfo?, response: ServerResponse?) {
        if (response?.bodyAsString == null) {
            onError(
                context,
                uploadInfo,
                response,
                Exception("Upload was successful but server response body was null")
            )
            return
        }
        try {
            val image = Gson().fromJson<ImgurResponse<AlbumImage>>(response.bodyAsString)
            if (image?.data != null) {
                albumDetailsRepository.updateUploadSuccess(
                    imageKey = uploadInfo?.uploadId,
                    responseImage = image.data
                )
            }
        } catch (e: Exception) {
            onError(context, uploadInfo, response, e)
        }
    }

    override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
        albumDetailsRepository.updateUploadCanceled(uploadInfo?.uploadId)
    }
}
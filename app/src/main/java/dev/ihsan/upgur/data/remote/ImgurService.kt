package dev.ihsan.upgur.data.remote

import androidx.lifecycle.LiveData
import dev.ihsan.upgur.data.model.*
import retrofit2.http.*

interface ImgurService {

    @GET("account/{username}/avatar")
    fun accountAvatar(
        @Header("Authorization") value: String,
        @Path("username") username: String?
    ): LiveData<ApiResponse<AccountAvatar>>

    @GET("account/{username}/albums")
    fun albums(
        @Header("Authorization") value: String,
        @Path("username") username: String?
    ): LiveData<ApiResponse<List<Album>>>

    @GET("album/{album_id}/images")
    fun albumImages(
        @Header("Authorization") value: String,
        @Path("album_id") albumId: String?
    ): LiveData<ApiResponse<List<AlbumImage>>>
}
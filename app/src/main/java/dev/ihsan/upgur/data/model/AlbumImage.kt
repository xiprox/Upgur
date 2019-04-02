package dev.ihsan.upgur.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AlbumImage(
    @PrimaryKey var id: String,
    @ColumnInfo var title: String? = null,
    @ColumnInfo var description: String? = null,
    @ColumnInfo var datetime: Int?,
    @ColumnInfo var animated: Boolean? = null,
    @ColumnInfo var width: Int? = null,
    @ColumnInfo var height: Int? = null,
    @ColumnInfo var size: Int? = null,
    @ColumnInfo var views: Int? = null,
    @ColumnInfo var link: String?,

    @ColumnInfo(name = "album_id") var albumId: String?,
    @ColumnInfo(name = "queued_for_upload") var queuedForUpload: Boolean = false,
    @ColumnInfo(name = "uploaded_percentage") var uploadedPercentage: Int? = null,
    @ColumnInfo(name = "upload_id") var uploadId: String? = null,
    @ColumnInfo(name = "upload_error") var uploadError: String? = null
)
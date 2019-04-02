package dev.ihsan.upgur.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Album(
    @PrimaryKey var id: String,
    @ColumnInfo var title: String?,
    @ColumnInfo var description: String?,
    @ColumnInfo var datetime: Int?,
    @ColumnInfo var cover: String?,
    @ColumnInfo var privacy: String?,
    @ColumnInfo var views: Int?,
    @ColumnInfo var link: String?,
    @ColumnInfo var favorite: Boolean?,
    @ColumnInfo var images_count: Int?,
    @ColumnInfo var is_album: Boolean?
)
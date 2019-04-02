package dev.ihsan.upgur.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.ihsan.upgur.data.model.Album
import dev.ihsan.upgur.data.model.AlbumImage

@Database(
    entities = [Album::class, AlbumImage::class], version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun imageDao(): ImageDao
}
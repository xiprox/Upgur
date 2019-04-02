package dev.ihsan.upgur.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.ihsan.upgur.data.model.AlbumImage

@Dao
abstract class ImageDao {
    @Query("SELECT * FROM AlbumImage")
    abstract fun getAll(): LiveData<List<AlbumImage>>

    @Query("SELECT * FROM AlbumImage WHERE id IN (:keys)")
    abstract fun findAllByKeysOnce(keys: List<String>): List<AlbumImage>

    @Query("SELECT * FROM AlbumImage WHERE id = :key LIMIT 1")
    abstract fun findFirstByKeyOnce(key: String): AlbumImage?

    @Query("SELECT * FROM AlbumImage WHERE album_id IS :albumId")
    abstract fun findAllByAlbumId(albumId: String): LiveData<List<AlbumImage>>

    @Query("SELECT * FROM AlbumImage WHERE queued_for_upload = 1")
    abstract fun findAllPendingUploads(): List<AlbumImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(image: AlbumImage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(images: List<AlbumImage>)

    @Update
    abstract fun update(image: AlbumImage)

    @Delete
    abstract fun delete(image: AlbumImage)
}
package dev.ihsan.upgur.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.ihsan.upgur.data.model.Album

@Dao
abstract class AlbumDao {
    @Query("SELECT * FROM Album")
    abstract fun getAll(): LiveData<List<Album>>

    @Query("SELECT * FROM Album WHERE id IS :id LIMIT 1")
    abstract fun findById(id: String): LiveData<Album>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(images: List<Album>)

    @Delete
    abstract fun delete(image: Album)
}
package dev.ihsan.upgur.ui.albums

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import dev.ihsan.upgur.AppExecutors
import dev.ihsan.upgur.Injector
import dev.ihsan.upgur.data.config.Credentials
import dev.ihsan.upgur.data.db.AppDatabase
import dev.ihsan.upgur.data.model.AccountAvatar
import dev.ihsan.upgur.data.model.Album
import dev.ihsan.upgur.data.remote.Resource
import javax.inject.Inject

class AlbumsViewModel : ViewModel() {
    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var credentials: Credentials

    @Inject
    lateinit var executors: AppExecutors

    @Inject
    lateinit var albumsRepository: AlbumsRepository

    init {
        Injector.get().inject(this)
    }

    // Dummy dependency to be able to trigger reloads.
    // Wish I knew better, but right now this is all we'll get.
    private val _dummyDependency: MutableLiveData<String> = MutableLiveData()

    val avatar: LiveData<Resource<AccountAvatar>> =
        Transformations.switchMap(_dummyDependency) { albumsRepository.loadAvatar() }

    val albums: LiveData<Resource<List<Album>>> =
        Transformations.switchMap(_dummyDependency) { albumsRepository.loadAlbums() }

    val username: LiveData<String> = MutableLiveData<String>().also { it.value = credentials.username }

    fun load() {
        _dummyDependency.value = "dumb"
    }

    fun reload(forceRefetch: Boolean = false) {
        if (forceRefetch) {
            albumsRepository.resetRatelimit()
        }
        _dummyDependency.value = _dummyDependency.value
    }

    fun logout() {
        credentials.logout()
        executors.diskIO().execute {
            appDatabase.clearAllTables()
        }
    }
}
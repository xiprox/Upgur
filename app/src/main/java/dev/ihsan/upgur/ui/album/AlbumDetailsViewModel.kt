package dev.ihsan.upgur.ui.album

import androidx.lifecycle.*
import dev.ihsan.upgur.Injector
import dev.ihsan.upgur.data.model.Album
import dev.ihsan.upgur.data.model.AlbumImage
import dev.ihsan.upgur.data.remote.upload.ImageUploader
import dev.ihsan.upgur.data.remote.Resource
import javax.inject.Inject

class AlbumDetailsViewModel : ViewModel() {

    @Inject
    lateinit var albumDetailsRepository: AlbumDetailsRepository

    init {
        Injector.get().inject(this)
    }

    private val _albumId: MutableLiveData<String> = MutableLiveData()

    val sortingOrder = MutableLiveData<SortingOrder>().also { it.value = SortingOrder.DESCENDING }

    val album: LiveData<Resource<Album>> = Transformations.switchMap(_albumId) {
        albumDetailsRepository.loadAlbum(it)
    }

    val images: LiveData<Resource<List<AlbumImage>>> = Transformations.switchMap(_albumId) { albId ->
        Transformations.switchMap(sortingOrder) { order ->
            Transformations.map(albumDetailsRepository.loadImages(albId)) { r ->
                r.data =
                    if (order == SortingOrder.ASCENDING) {
                        r.data?.sortedBy { it.datetime }
                    } else {
                        r.data?.sortedByDescending { it.datetime }
                    }
                r
            }
        }
    }

    fun uploadImages(urls: List<String>) {
        if (_albumId.value == null) return
        albumDetailsRepository.addPendingUploads(_albumId.value!!, urls) {
            ImageUploader.enqueue()
        }
    }

    fun setSortingOrder(order: SortingOrder) {
        sortingOrder.value = order
    }

    fun load(id: String?) {
        _albumId.value = id
    }

    fun reload(forceRefetch: Boolean = false) {
        if (forceRefetch) {
            albumDetailsRepository.resetRatelimit(_albumId.value ?: "")
        }
        load(_albumId.value)
    }

    enum class SortingOrder {
        ASCENDING, DESCENDING
    }
}
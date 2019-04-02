package dev.ihsan.upgur.ui.album

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.asksira.bsimagepicker.BSImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dev.ihsan.upgur.App

import dev.ihsan.upgur.R
import dev.ihsan.upgur.data.model.Album
import dev.ihsan.upgur.data.model.AlbumImage
import dev.ihsan.upgur.data.remote.Resource
import dev.ihsan.upgur.ui.widget.ItemOffsetDecoration
import dev.ihsan.upgur.ui.widget.OnItemClickListener
import kotlinx.android.synthetic.main.fragment_album_details.*
import java.util.*
import kotlin.concurrent.schedule
import androidx.activity.OnBackPressedCallback

class AlbumDetailsFragment : Fragment(), BSImagePicker.OnMultiImageSelectedListener, OnBackPressedCallback {
    private var albumId: String? = null

    private lateinit var viewModel: AlbumDetailsViewModel

    private var ascendingSortMenuItem: MenuItem? = null
    private var descendingSortMenuItem: MenuItem? = null

    private val adapter = ImagesAdapter(object : OnItemClickListener<AlbumImage> {
        override fun onItemClick(item: AlbumImage) {
            onImageClick(item)
        }
    })

    private lateinit var errorBottomSheetBehavior: BottomSheetBehavior<LinearLayoutCompat>

    override fun handleOnBackPressed(): Boolean {
        if (errorBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            errorBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            albumId = it.getString(ARG_ALBUM_ID)
        }
        viewModel = activity?.run {
            ViewModelProviders.of(this).get(AlbumDetailsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.album.observe(this, Observer {
            onAlbumUpdated(it)
        })
        viewModel.images.observe(this, Observer {
            onImagesUpdated(it)
        })
        viewModel.sortingOrder.observe(this, Observer {
            onSortingOrderChanged(it)
        })
        return inflater.inflate(R.layout.fragment_album_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up callback to handle back presses. Currently only used for hiding expanded bottom sheet.
        requireActivity().addOnBackPressedCallback(viewLifecycleOwner, this)

        // Set up toolbar nav
        toolbar.setupWithNavController(findNavController())

        // Set up sort menu item
        ascendingSortMenuItem = toolbar.menu.findItem(R.id.action_sort_ascending)
        descendingSortMenuItem = toolbar.menu.findItem(R.id.action_sort_descending)
        ascendingSortMenuItem?.setOnMenuItemClickListener {
            viewModel.setSortingOrder(AlbumDetailsViewModel.SortingOrder.ASCENDING)
            true
        }
        descendingSortMenuItem?.setOnMenuItemClickListener {
            viewModel.setSortingOrder(AlbumDetailsViewModel.SortingOrder.DESCENDING)
            true
        }

        // Set up RecyclerView and its adapter
        recycler.adapter = adapter
        adapter.submitList(arrayListOf())
        recycler.addItemDecoration(ItemOffsetDecoration(context, R.dimen.album_grid_spacing))

        // ErrorView retry
        errorView.setRetryListener {
            viewModel.reload()
        }

        // Swipe Refresh
        swipeRefresh.setOnRefreshListener {
            viewModel.reload(true)
        }

        // Upload FAB
        uploadButton.setOnClickListener {
            openUploadSheet()
        }

        // Bottom Sheet
        errorBottomSheetBehavior = BottomSheetBehavior.from(errorBottomSheet)
        errorBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Load data initially
        viewModel.load(albumId)
    }

    private fun onAlbumUpdated(it: Resource<Album>) {
        toolbarLayout.title = it.data?.title ?: getString(R.string.title_common_untitled)
    }

    private fun onImagesUpdated(it: Resource<List<AlbumImage>>) {
        if (it.data?.isNotEmpty() == true) {
            adapter.submitList(it.data!!)
        }
        if (it.status == Resource.Status.ERROR) {
            errorView.setSubtitle("${errorView.subtitle}\n\n${it.message}")
        }
        lceeView.showBasedOnResource(it, usingSwipeRefresh = true)
        swipeRefresh.isRefreshing = it.status == Resource.Status.LOADING
    }

    private fun onSortingOrderChanged(it: AlbumDetailsViewModel.SortingOrder) {
        val isAscending = it == AlbumDetailsViewModel.SortingOrder.ASCENDING
        val isDescending = it == AlbumDetailsViewModel.SortingOrder.DESCENDING
        ascendingSortMenuItem?.isCheckable = isAscending
        ascendingSortMenuItem?.isChecked = isAscending
        descendingSortMenuItem?.isCheckable = isDescending
        descendingSortMenuItem?.isChecked = isDescending
        smoothScrollToTopOfRecyclerDelayed()
    }

    private fun onImageClick(item: AlbumImage) {
        if (item.uploadError != null) {
            // Key of temporary images is file path
            openUploadErrorSheet(item.id, item.uploadError!!)
        }
    }

    override fun onMultiImageSelected(uriList: MutableList<Uri>?, tag: String?) {
        if (uriList == null) return
        uploadImages(uriList.map { it.toString() })
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun openUploadSheet() {
        val picker = BSImagePicker.Builder("${App.get().packageName}.fileprovider")
            .isMultiSelect()
            .setMaximumMultiSelectCount(20)
            .setMultiSelectDoneTextColor(R.color.colorAccent)
            .build()
        picker.show(childFragmentManager, "image_picker")
    }

    private fun openUploadErrorSheet(fileUrl: String, error: String) {
        errorMessage.text = error
        retryButton.setOnClickListener {
            errorBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            uploadImages(listOf(fileUrl))
        }
        errorBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun uploadImages(urls: List<String>) {
        viewModel.uploadImages(urls)
        smoothScrollToTopOfRecyclerDelayed()
    }

    private fun smoothScrollToTopOfRecyclerDelayed(delay: Long = 400) {
        Timer("ScrollToTop", false).schedule(delay) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                recycler.post { recycler.smoothScrollToPosition(0) }
            }
        }
    }

    companion object {
        const val ARG_ALBUM_ID = "albumId"
    }
}

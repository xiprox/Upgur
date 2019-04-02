package dev.ihsan.upgur.ui.albums

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dev.ihsan.upgur.R
import dev.ihsan.upgur.data.model.Album
import dev.ihsan.upgur.data.remote.Resource
import dev.ihsan.upgur.ui.album.AlbumDetailsFragment
import dev.ihsan.upgur.ui.login.LoginActivity
import dev.ihsan.upgur.ui.widget.ItemOffsetDecoration
import dev.ihsan.upgur.ui.widget.OnItemClickListener
import kotlinx.android.synthetic.main.fragment_albums.*

class AlbumsFragment : Fragment(), OnBackPressedCallback {
    private lateinit var viewModel: AlbumsViewModel

    private val adapter = AlbumsAdapter(object : OnItemClickListener<Album> {
        override fun onItemClick(item: Album) {
            onAlbumClick(item)
        }
    })

    private lateinit var userDetailsSheetBehavior: BottomSheetBehavior<LinearLayoutCompat>

    override fun handleOnBackPressed(): Boolean {
        if (userDetailsSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            userDetailsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this).get(AlbumsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.username.observe(this, Observer {
            username.text = it ?: "?"
        })
        viewModel.avatar.observe(this, Observer {
            if (it.data == null) return@Observer
            toolbarAvatar.setImageURI(it.data?.avatar)
            bottomSheetAvatar.setImageURI(it.data?.avatar)
        })
        viewModel.albums.observe(this, Observer {
            onAlbumsUpdated(it)
        })
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up callback to handle back presses. Currently only used for hiding expanded bottom sheet.
        requireActivity().addOnBackPressedCallback(viewLifecycleOwner, this)

        // Toolbar avatar view
        toolbarAvatar.setOnClickListener {
            userDetailsSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // RecyclerView setup
        recycler.adapter = adapter
        adapter.submitList(arrayListOf())
        recycler.addItemDecoration(ItemOffsetDecoration(context, R.dimen.album_grid_spacing))

        // Error view
        errorView.setRetryListener {
            viewModel.reload()
        }

        // Swipe Refresh
        swipeRefresh.setOnRefreshListener {
            viewModel.reload(true)
        }

        // Bottom Sheet
        userDetailsSheetBehavior = BottomSheetBehavior.from(userDetailsSheet)
        userDetailsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        logoutButton.setOnClickListener {
            logout()
        }

        // Load data initially
        viewModel.load()
    }

    private fun onAlbumsUpdated(it: Resource<List<Album>>) {
        if (it.data?.isNotEmpty() == true) {
            adapter.submitList(it.data!!)
        }
        if (it.status == Resource.Status.ERROR) {
            errorView.setSubtitle("${errorView.subtitle}\n\n${it.message}")
        }
        lceeView.showBasedOnResource(it, usingSwipeRefresh = true)
        swipeRefresh.isRefreshing = it.status == Resource.Status.LOADING
    }

    private fun onAlbumClick(item: Album) {
        val nav = findNavController()
        if (nav.currentDestination?.id == R.id.albumDetailsFragment) return
        nav.navigate(
            R.id.action_albumsFragment_to_albumDetailsFragment,
            bundleOf(
                AlbumDetailsFragment.ARG_ALBUM_ID to item.id
            )
        )
    }

    private fun logout() {
        viewModel.logout()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity?.startActivity(intent)
    }
}

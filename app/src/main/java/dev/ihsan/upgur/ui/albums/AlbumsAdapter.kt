package dev.ihsan.upgur.ui.albums

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.ihsan.upgur.R
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import dev.ihsan.upgur.data.model.Album
import dev.ihsan.upgur.util.ImgurUtils
import kotlinx.android.synthetic.main.item_album.view.*
import dev.ihsan.upgur.ui.widget.OnItemClickListener

class AlbumsAdapter(private val onItemClickListener: OnItemClickListener<Album>) :
    RecyclerView.Adapter<AlbumsAdapter.ViewHolder>() {
    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(item) }
        holder.view.let { v ->
            v.coverImage.setImageURI(ImgurUtils.coverImageUrlFromId(item.cover ?: ""))
            v.albumName.text = item.title
            v.privacyIcon.setImageResource(
                if (item.privacy == "public") {
                    R.drawable.ic_privacy_visible
                } else {
                    R.drawable.ic_privacy_hidden
                }
            )
            val imageCount = item.images_count
            if (imageCount != null) {
                v.imagesCount.text = v.context.resources.getQuantityString(
                    R.plurals.label_albums_image_count,
                    imageCount,
                    imageCount
                )
            }
        }
    }

    fun submitList(list: List<Album>) {
        differ.submitList(list)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    companion object {
        internal val diffCallback = object : DiffUtil.ItemCallback<Album>() {
            override fun areItemsTheSame(oldAlbum: Album, newAlbum: Album): Boolean {
                return oldAlbum.id == newAlbum.id
            }

            override fun areContentsTheSame(oldAlbum: Album, newAlbum: Album): Boolean {
                return oldAlbum == newAlbum
            }
        }
    }
}
package dev.ihsan.upgur.ui.album

import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.ihsan.upgur.R
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import dev.ihsan.upgur.data.model.AlbumImage
import dev.ihsan.upgur.ui.widget.OnItemClickListener
import kotlinx.android.synthetic.main.item_image.view.*

class ImagesAdapter(private val onItemClickListener: OnItemClickListener<AlbumImage>) :
    RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(item) }
        holder.view.let {
            it.image.setImageURI(item.link)

            when {
                item.uploadError != null -> {
                    it.statusIndicatorContainer.visibility = VISIBLE
                    it.progressBar.visibility = GONE
                    it.errorOverlay.visibility = VISIBLE
                    it.uploadingOverlay.visibility = INVISIBLE
                    it.statusIndicatorIcon.setImageResource(R.drawable.ic_error_outline_white_24dp)
                }
                item.queuedForUpload -> {
                    it.statusIndicatorContainer.visibility = VISIBLE
                    it.progressBar.visibility = if (item.uploadedPercentage != null) VISIBLE else GONE
                    it.errorOverlay.visibility = INVISIBLE
                    it.uploadingOverlay.visibility = VISIBLE
                    it.statusIndicatorIcon.setImageResource(R.drawable.ic_upload_white_24dp)
                    it.progressBar.progress = item.uploadedPercentage ?: 0
                }
                else -> {
                    it.statusIndicatorContainer.visibility = INVISIBLE
                    it.progressBar.visibility = GONE
                    it.uploadingOverlay.visibility = INVISIBLE
                    it.errorOverlay.visibility = INVISIBLE
                }
            }
        }
    }

    fun submitList(list: List<AlbumImage>) {
        differ.submitList(list)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    companion object {
        internal val diffCallback = object : DiffUtil.ItemCallback<AlbumImage>() {
            override fun areItemsTheSame(oldAlbum: AlbumImage, newAlbum: AlbumImage): Boolean {
                return oldAlbum.id == newAlbum.id
            }

            override fun areContentsTheSame(oldAlbum: AlbumImage, newAlbum: AlbumImage): Boolean {
                return oldAlbum == newAlbum
            }
        }
    }
}
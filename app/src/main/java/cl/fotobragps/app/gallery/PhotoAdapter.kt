package cl.fotobragps.app.gallery

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cl.fotobragps.app.databinding.ItemPhotoBinding

class PhotoAdapter(
    private val onClick: (Uri) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.Holder>() {

    private val items = mutableListOf<Uri>()

    fun submit(newItems: List<Uri>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(
        private val binding: ItemPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            val resolver = binding.imagePhoto.context.contentResolver

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val thumb = resolver.loadThumbnail(
                        uri,
                        android.util.Size(420, 420),
                        null
                    )
                    binding.imagePhoto.setImageBitmap(thumb)
                } else {
                    @Suppress("DEPRECATION")
                    val bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                        resolver,
                        uri.lastPathSegment?.toLongOrNull() ?: 0L,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null
                    )
                    binding.imagePhoto.setImageBitmap(bitmap)
                }
            } catch (_: Exception) {
                binding.imagePhoto.setImageURI(uri)
            }

            binding.root.setOnClickListener {
                onClick(uri)
            }
        }
    }
}

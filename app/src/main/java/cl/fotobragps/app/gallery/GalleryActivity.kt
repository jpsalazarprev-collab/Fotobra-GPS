package cl.fotobragps.app.gallery

import android.content.Intent
import android.net.Uri
import android.content.ActivityNotFoundException
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import cl.fotobragps.app.databinding.ActivityGalleryBinding

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding
    private lateinit var adapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PhotoAdapter { uri ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "image/jpeg")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "No hay una aplicación disponible para abrir la foto.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.recyclerGallery.layoutManager =
            GridLayoutManager(this, 3)
        binding.recyclerGallery.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adapter.submit(PhotoStore.loadFotobraPhotos(this))
    }
}

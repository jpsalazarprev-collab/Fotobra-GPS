package cl.fotobragps.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import cl.fotobragps.app.camera.LocationSnapshot
import cl.fotobragps.app.camera.LocationTracker
import cl.fotobragps.app.camera.PhotoStampRenderer
import cl.fotobragps.app.databinding.ActivityMainBinding
import cl.fotobragps.app.gallery.GalleryActivity
import cl.fotobragps.app.settings.SettingsActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var geocodeExecutor: ExecutorService
    private lateinit var locationTracker: LocationTracker

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var location: LocationSnapshot? = null
    private var addressText: String = "Ubicación no disponible"
    private var flashEnabled = false

    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockRunnable = object : Runnable {
        override fun run() {
            updateClock()
            clockHandler.postDelayed(this, 1000)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val cameraGranted = result[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        if (cameraGranted) {
            startCamera()
        } else {
            Toast.makeText(
                this,
                "Fotobra GPS necesita permiso de cámara.",
                Toast.LENGTH_LONG
            ).show()
        }

        refreshLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        geocodeExecutor = Executors.newSingleThreadExecutor()
        locationTracker = LocationTracker(this)

        binding.btnCapture.setOnClickListener { capturePhoto() }
        binding.btnGallery.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnSwitchCamera.setOnClickListener {
            flashEnabled = false
            camera?.cameraControl?.enableTorch(false)

            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera()
        }
        binding.btnFlash.setOnClickListener {
            val activeCamera = camera
            if (activeCamera?.cameraInfo?.hasFlashUnit() != true) {
                flashEnabled = false
                Toast.makeText(
                    this,
                    "Esta cámara no tiene flash disponible.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            flashEnabled = !flashEnabled
            activeCamera.cameraControl.enableTorch(flashEnabled)
            Toast.makeText(
                this,
                if (flashEnabled) "Flash encendido" else "Flash apagado",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.etNote.setOnEditorActionListener { _, _, _ ->
            binding.tvNote.text = binding.etNote.text.toString()
            false
        }

        requestPermissionsAndStart()
    }

    override fun onResume() {
        super.onResume()
        applyPreferences()
        clockHandler.post(clockRunnable)
        refreshLocation()
    }

    override fun onPause() {
        super.onPause()
        clockHandler.removeCallbacks(clockRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        geocodeExecutor.shutdown()
    }

    private fun requestPermissionsAndStart() {
        val missing = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            missing += Manifest.permission.CAMERA
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            missing += Manifest.permission.ACCESS_FINE_LOCATION
            missing += Manifest.permission.ACCESS_COARSE_LOCATION
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missing += Manifest.permission.WRITE_EXTERNAL_STORAGE
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missing += Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        if (missing.isEmpty()) {
            startCamera()
            refreshLocation()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)

        providerFuture.addListener({
            try {
                val provider = providerFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.previewView.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                val selector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    this,
                    selector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "No se pudo iniciar la cámara: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun refreshLocation() {
        locationTracker.getCurrentLocation { snapshot ->
            location = snapshot

            if (snapshot == null) {
                binding.tvGps.text = "GPS: no disponible"
                binding.tvAddress.text = "Ubicación no disponible"
                addressText = "Ubicación no disponible"
                return@getCurrentLocation
            }

            binding.tvGps.text =
                "GPS ${"%.6f".format(Locale.US, snapshot.latitude)}, " +
                "${"%.6f".format(Locale.US, snapshot.longitude)} · " +
                "±${snapshot.accuracy.toInt()} m"

            reverseGeocode(snapshot)
        }
    }

    private fun reverseGeocode(snapshot: LocationSnapshot) {
        geocodeExecutor.execute {
            val resolved = try {
                val geocoder = Geocoder(
                    this,
                    Locale.forLanguageTag("es-CL")
                )

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(
                    snapshot.latitude,
                    snapshot.longitude,
                    1
                )

                val first = addresses?.firstOrNull()
                if (
                    first == null ||
                    first.getAddressLine(0).isNullOrBlank()
                ) {
                    fallbackCoordinates(snapshot)
                } else {
                    first.getAddressLine(0)
                }
            } catch (_: Exception) {
                fallbackCoordinates(snapshot)
            }

            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    addressText = resolved
                    binding.tvAddress.text = resolved
                }
            }
        }
    }

    private fun fallbackCoordinates(snapshot: LocationSnapshot): String {
        return "${"%.6f".format(Locale.US, snapshot.latitude)}, " +
            "${"%.6f".format(Locale.US, snapshot.longitude)}"
    }

    private fun updateClock() {
        val now = Date()
        val locale = Locale.forLanguageTag("es-CL")

        binding.tvTime.text = SimpleDateFormat("HH:mm", locale).format(now)
        binding.tvDate.text = SimpleDateFormat("dd MMM yyyy", locale).format(now)
        binding.tvDay.text = SimpleDateFormat("EEEE", locale).format(now)
        binding.tvNote.text = binding.etNote.text.toString()

        applyPreferences()
    }

    private fun applyPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val brand = prefs.getString("brand_text", "Fotobra GPS") ?: "Fotobra GPS"

        binding.tvBrand.text = brand
        binding.tvTime.visibility =
            if (prefs.getBoolean("show_time", true)) android.view.View.VISIBLE
            else android.view.View.GONE
        binding.tvDate.visibility =
            if (prefs.getBoolean("show_date", true)) android.view.View.VISIBLE
            else android.view.View.GONE
        binding.tvDay.visibility = binding.tvDate.visibility
        binding.tvAddress.visibility =
            if (prefs.getBoolean("show_address", true)) android.view.View.VISIBLE
            else android.view.View.GONE
        binding.tvGps.visibility =
            if (prefs.getBoolean("show_gps", true)) android.view.View.VISIBLE
            else android.view.View.GONE
        binding.tvNote.visibility =
            if (prefs.getBoolean("show_note", true)) android.view.View.VISIBLE
            else android.view.View.GONE
    }

    private fun capturePhoto() {
        val capture = imageCapture ?: return

        if (!binding.btnCapture.isEnabled) {
            return
        }

        binding.btnCapture.isEnabled = false
        binding.btnCapture.alpha = 0.55f
        binding.tvNote.text = binding.etNote.text.toString()

        // Refresca la ubicación inmediatamente antes de tomar la foto.
        locationTracker.getCurrentLocation { freshLocation ->
            if (freshLocation != null) {
                location = freshLocation
            }

            val capturedLocation = location
            val capturedAddress = addressText
            val capturedNote = binding.etNote.text.toString().trim()

            val tempFile = File.createTempFile(
                "fotobra_capture_",
                ".jpg",
                cacheDir
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile)
                .build()

            capture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(
                        outputFileResults: ImageCapture.OutputFileResults
                    ) {
                        try {
                            val uri = PhotoStampRenderer.processAndSave(
                                context = this@MainActivity,
                                sourceFile = tempFile,
                                location = capturedLocation,
                                address = capturedAddress,
                                note = capturedNote
                            )

                            runOnUiThread {
                                binding.btnCapture.isEnabled = true
                                binding.btnCapture.alpha = 1f

                                Toast.makeText(
                                    this@MainActivity,
                                    if (uri != null)
                                        "Foto guardada en Fotobra GPS"
                                    else
                                        "No se pudo guardar la foto",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                binding.btnCapture.isEnabled = true
                                binding.btnCapture.alpha = 1f

                                Toast.makeText(
                                    this@MainActivity,
                                    "Error procesando foto: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } finally {
                            tempFile.delete()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        runOnUiThread {
                            binding.btnCapture.isEnabled = true
                            binding.btnCapture.alpha = 1f

                            Toast.makeText(
                                this@MainActivity,
                                "Error de cámara: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
        }
    }
}

package cl.fotobragps.app.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.concurrent.atomic.AtomicBoolean

data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestampMillis: Long
)

class LocationTracker(
    private val context: Context
) {
    private val client =
        LocationServices.getFusedLocationProviderClient(context)

    private val mainHandler = Handler(Looper.getMainLooper())

    fun getCurrentLocation(
        timeoutMillis: Long = 10_000L,
        callback: (LocationSnapshot?) -> Unit
    ) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            callbackOnMain(callback, null)
            return
        }

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            callbackOnMain(callback, null)
            return
        }

        val prefs =
            PreferenceManager.getDefaultSharedPreferences(context)
        val highAccuracy =
            prefs.getBoolean("high_accuracy", true)

        val priority =
            if (highAccuracy && fineGranted) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

        val completed = AtomicBoolean(false)
        val tokenSource = CancellationTokenSource()

        fun finish(snapshot: LocationSnapshot?) {
            if (completed.compareAndSet(false, true)) {
                tokenSource.cancel()
                callbackOnMain(callback, snapshot)
            }
        }

        val timeout = Runnable {
            if (completed.get()) return@Runnable

            try {
                client.lastLocation
                    .addOnSuccessListener { last ->
                        finish(last?.let(::toSnapshot))
                    }
                    .addOnFailureListener {
                        finish(null)
                    }
            } catch (_: SecurityException) {
                finish(null)
            }
        }

        mainHandler.postDelayed(timeout, timeoutMillis)

        try {
            client.getCurrentLocation(
                priority,
                tokenSource.token
            )
                .addOnSuccessListener { current ->
                    if (current != null) {
                        mainHandler.removeCallbacks(timeout)
                        finish(toSnapshot(current))
                    }
                }
                .addOnFailureListener {
                    // Espera el timeout para intentar lastLocation.
                }
        } catch (_: SecurityException) {
            mainHandler.removeCallbacks(timeout)
            finish(null)
        }
    }

    private fun toSnapshot(
        location: android.location.Location
    ): LocationSnapshot {
        return LocationSnapshot(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            timestampMillis = location.time
        )
    }

    private fun callbackOnMain(
        callback: (LocationSnapshot?) -> Unit,
        value: LocationSnapshot?
    ) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback(value)
        } else {
            mainHandler.post {
                callback(value)
            }
        }
    }
}

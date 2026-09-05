package cl.fotobragps.app.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.preference.PreferenceManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object PhotoStampRenderer {

    fun processAndSave(
        context: Context,
        sourceFile: File,
        location: LocationSnapshot?,
        address: String,
        note: String
    ): Uri? {
        val bitmap = decodeCorrectlyOriented(sourceFile) ?: return null
        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        drawStamp(
            canvas = canvas,
            bitmap = mutable,
            brand = prefs.getString("brand_text", "Fotobra GPS") ?: "Fotobra GPS",
            location = location,
            address = address,
            note = note,
            showTime = prefs.getBoolean("show_time", true),
            showDate = prefs.getBoolean("show_date", true),
            showAddress = prefs.getBoolean("show_address", true),
            showGps = prefs.getBoolean("show_gps", true),
            showNote = prefs.getBoolean("show_note", true)
        )

        if (prefs.getBoolean("save_original", false)) {
            saveBitmap(
                context = context,
                bitmap = bitmap,
                suffix = "_original"
            )
        }

        val savedUri = saveBitmap(
            context = context,
            bitmap = mutable,
            suffix = ""
        )

        if (mutable !== bitmap) {
            mutable.recycle()
        }
        bitmap.recycle()

        return savedUri
    }

    private fun drawStamp(
        canvas: Canvas,
        bitmap: Bitmap,
        brand: String,
        location: LocationSnapshot?,
        address: String,
        note: String,
        showTime: Boolean,
        showDate: Boolean,
        showAddress: Boolean,
        showGps: Boolean,
        showNote: Boolean
    ) {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val scale = max(0.75f, width / 1600f)

        val panelHeight = (280f * scale).coerceAtMost(height * 0.36f)
        val top = height - panelHeight

        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(170, 0, 0, 0)
        }
        canvas.drawRect(0f, top, width, height, background)

        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val gray = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 220, 220)
        }
        val yellow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 193, 7)
        }

        val now = Date()
        val locale = Locale.forLanguageTag("es-CL")

        val time = SimpleDateFormat("HH:mm", locale).format(now)
        val date = SimpleDateFormat("dd MMM yyyy", locale).format(now)
        val day = SimpleDateFormat("EEEE", locale).format(now)

        val left = 28f * scale
        val timeY = top + 82f * scale

        if (showTime) {
            white.textSize = 62f * scale
            white.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(time, left, timeY, white)
        }

        val dividerX = left + 210f * scale
        yellow.strokeWidth = 7f * scale
        canvas.drawLine(
            dividerX,
            top + 26f * scale,
            dividerX,
            top + 125f * scale,
            yellow
        )

        if (showDate) {
            white.textSize = 27f * scale
            white.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(
                date,
                dividerX + 24f * scale,
                top + 63f * scale,
                white
            )

            gray.textSize = 22f * scale
            gray.typeface = Typeface.DEFAULT
            canvas.drawText(
                day,
                dividerX + 24f * scale,
                top + 101f * scale,
                gray
            )
        }

        var y = top + 168f * scale

        if (showAddress) {
            white.textSize = 23f * scale
            white.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(
                clipText(address, 68),
                left,
                y,
                white
            )
            y += 36f * scale
        }

        if (showGps) {
            yellow.textSize = 20f * scale
            yellow.typeface = Typeface.DEFAULT_BOLD

            val gpsText = if (location == null) {
                "GPS no disponible"
            } else {
                "GPS ${"%.6f".format(Locale.US, location.latitude)}, " +
                    "${"%.6f".format(Locale.US, location.longitude)}  " +
                    "±${location.accuracy.toInt()} m"
            }

            canvas.drawText(
                clipText(gpsText, 70),
                left,
                y,
                yellow
            )
            y += 34f * scale
        }

        if (showNote && note.isNotBlank()) {
            white.textSize = 20f * scale
            white.typeface = Typeface.DEFAULT
            canvas.drawText(
                clipText(note, 74),
                left,
                y,
                white
            )
        }

        // Bloque de marca superior derecha, con diseño propio Fotobra GPS.
        val brandWidth = 360f * scale
        val brandHeight = 96f * scale

        canvas.drawRect(
            width - brandWidth,
            0f,
            width,
            brandHeight,
            background
        )

        yellow.textSize = 34f * scale
        yellow.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(
            clipText(brand, 22),
            width - brandWidth + 18f * scale,
            47f * scale,
            yellow
        )

        white.textSize = 17f * scale
        white.typeface = Typeface.DEFAULT
        canvas.drawText(
            "Fecha · hora · ubicación · GPS",
            width - brandWidth + 18f * scale,
            76f * scale,
            white
        )
    }

    private fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        suffix: String
    ): Uri? {
        val stamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.US
        ).format(Date())

        val fileName = "FotobraGPS_${stamp}${suffix}.jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/Fotobra GPS"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: return null

        return try {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    94,
                    out
                )
            } ?: return null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val done = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                resolver.update(uri, done, null, null)
            }

            uri
        } catch (_: Exception) {
            resolver.delete(uri, null, null)
            null
        }
    }

    private fun decodeCorrectlyOriented(file: File): Bitmap? {
        val decoded = BitmapFactory.decodeFile(file.absolutePath) ?: return null

        return try {
            val exif = ExifInterface(file)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (rotation == 0f) {
                decoded
            } else {
                val matrix = android.graphics.Matrix().apply {
                    postRotate(rotation)
                }
                val rotated = Bitmap.createBitmap(
                    decoded,
                    0,
                    0,
                    decoded.width,
                    decoded.height,
                    matrix,
                    true
                )
                if (rotated !== decoded) decoded.recycle()
                rotated
            }
        } catch (_: Exception) {
            decoded
        }
    }

    private fun clipText(
        text: String,
        maxChars: Int
    ): String {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        return if (clean.length <= maxChars) {
            clean
        } else {
            clean.take(maxChars - 1) + "…"
        }
    }
}

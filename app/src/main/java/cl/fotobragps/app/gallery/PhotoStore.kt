package cl.fotobragps.app.gallery

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

object PhotoStore {

    fun loadFotobraPhotos(
        context: Context
    ): List<Uri> {
        val result = mutableListOf<Uri>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val selection: String?
        val args: Array<String>?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            args = arrayOf("Pictures/Fotobra GPS%")
        } else {
            selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
            args = arrayOf("FotobraGPS_%")
        }

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            args,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media._ID
            )

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                result += ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
            }
        }

        return result
    }
}

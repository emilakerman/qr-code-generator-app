package com.emilakerman.qrcodegenereator

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

class BitMapUtils {
    // Convert the Bitmap to a ByteArray
     fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // You can change PNG to JPEG if you prefer
        return outputStream.toByteArray()
    }
}
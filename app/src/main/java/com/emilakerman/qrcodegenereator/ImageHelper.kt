package com.emilakerman.qrcodegenereator

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.emilakerman.qrcodegenereator.databinding.ActivityMainBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.transition.Transition

class ImageHelper {

    // Generates a QR code and displays in the binding.
    fun generateQrCode(binding: ActivityMainBinding) {
        val inputText = binding.inputField.text.toString()
        try {
            val encoder = BarcodeEncoder()
            // Generate the QR code bitmap
            val qrCodeBitmap = encoder.encodeBitmap(inputText, BarcodeFormat.QR_CODE, 250, 250)

            val mutableBitmap = qrCodeBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)

            val paint = Paint()
            paint.color = Color.BLACK  // Text color
            paint.textAlign = Paint.Align.CENTER // Center align the text

            val maxTextWidth = canvas.width * 0.9f
            var textSize = 30f
            paint.textSize = textSize

            while (paint.measureText(inputText) > maxTextWidth) {
                textSize -= 1f
                paint.textSize = textSize
            }

            val xPos = canvas.width / 2f
            val yPos = canvas.height - 15f

            canvas.drawText(inputText, xPos, yPos, paint)
            binding.qrCodeImage.setImageBitmap(mutableBitmap)
        } catch (e: WriterException) {
            println(e)
        }
    }


    // Downloads QR Code from URL.
    fun saveImageFromUrl(context: Context, imageUrl: String) {
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveMediaToStorage(resource, context)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    // Saves bitmap QR code to local storage.
    fun saveMediaToStorage(bitmap: Bitmap, context: Context) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(context, "QR Code Saved to Gallery!", Toast.LENGTH_SHORT).show()
        }
    }
}


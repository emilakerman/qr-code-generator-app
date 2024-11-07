package com.emilakerman.qrcodegenereator

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.emilakerman.qrcodegenereator.databinding.ActivityMainBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.generateButton.setOnClickListener {
            generateQrCode()
        }
        binding.saveButton.setOnClickListener {
            saveMediaToStorage(binding.qrCodeImage.drawToBitmap())
        }
        binding.clearButton.setOnClickListener {
            binding.inputField.text?.clear();
            binding.qrCodeImage.setImageResource(android.R.color.transparent);
        }
    }
    private fun generateQrCode() {
       val inputText = binding.inputField.text.toString()
        try {
            val encoder = BarcodeEncoder()
            val bitmap = encoder.encodeBitmap(inputText, BarcodeFormat.QR_CODE, 250, 250)
            binding.qrCodeImage.setImageBitmap(bitmap)
        } catch(e : WriterException) {
            println(e);
        }
    }
    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            contentResolver?.also { resolver ->
                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {
                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "QR Code Saved to Gallery!", Toast.LENGTH_SHORT).show()
        }
    }
}
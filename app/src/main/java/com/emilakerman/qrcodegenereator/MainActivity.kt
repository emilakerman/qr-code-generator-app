package com.emilakerman.qrcodegenereator

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.emilakerman.qrcodegenereator.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.saveProgressbar.visibility = View.GONE
        binding.generateProgressbar.visibility = View.GONE
        auth = FirebaseAuth.getInstance();


        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

        fun setupToolbar() {
            val toolbar: MaterialToolbar = binding.bottomAppBar;
            setSupportActionBar(binding.bottomAppBar)
            setSupportActionBar(toolbar)
            toolbar.title = ""
        }
        setupToolbar()

        binding.generateButton.setOnClickListener {
            if (binding.inputField.text.toString() == "") {
                return@setOnClickListener;
            } else {
                binding.generateProgressbar.visibility = View.VISIBLE
                binding.generateButton.text = ""
                generateQrCode().also {
                    // Adding a type of delay to simulate a loading state.
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.generateProgressbar.visibility = View.GONE
                            // TODO: Fix hardcoded string.
                            binding.generateButton.text = "Generate"
                        },
                        1000
                    )
                }
                it.hideKeyboard()
            }
        }
        binding.saveButton.setOnClickListener {
            if (binding.inputField.text.toString() == "") {
                return@setOnClickListener;
            } else {
                binding.saveProgressbar.visibility = View.VISIBLE
                binding.saveButton.text = ""
                saveMediaToStorage(binding.qrCodeImage.drawToBitmap()).also {
                    // Adding a type of delay to simulate a loading state.
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.saveProgressbar.visibility = View.GONE
                            // TODO: Fix hardcoded string.
                            binding.saveButton.text = "Save"
                        },
                        1000
                    )


                }
            }
        }
        binding.saveToCloud.setOnClickListener {
            if (binding.inputField.text.toString() == "") {
                return@setOnClickListener;
            } else {
                // save to cloud/vercel
                uploadImage(binding.qrCodeImage.drawToBitmap());
            }
        }
        binding.clearButton.setOnClickListener {
            binding.inputField.text?.clear();
            binding.qrCodeImage.setImageResource(android.R.color.transparent);
        }
    }
    private val client = OkHttpClient()

    // Convert the Bitmap to a ByteArray
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // You can change PNG to JPEG if you prefer
        return outputStream.toByteArray()
    }

    // In your ViewModel or non-UI class:
    private fun uploadImage(bitmap: Bitmap) {
        // Convert Bitmap to byte array
        val byteArray = bitmapToByteArray(bitmap)

        // Create a temporary file from the byte array
        val file = File.createTempFile("image", ".png")
        file.writeBytes(byteArray)

        // Build the multipart request body
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", file.name, file.asRequestBody("image/png".toMediaTypeOrNull())
            )
            .build()

        val keys = ApiKeys();

        // Create the request
        val request = Request.Builder()
            //NOTE: This works now but I turned off auth protection.
            .url("${keys.localHost}putQR")
            .addHeader("user", auth.currentUser?.uid.toString())
            .put(requestBody)
            .build()

        // Send the request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed with exception: ${e.message}")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println("Upload successful: ${response.body?.string()}")
                } else {
                    println("Upload failed with status code: ${response.code}")
                    println("Error details: ${response.body?.string()}")
                }
            }
        })
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bottom_app_bar_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val transaction = supportFragmentManager.beginTransaction()
        return when (item.itemId) {
            R.id.home -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                if (fragment != null) {
                    transaction.remove(fragment).commit()
                }
                true
            }
            R.id.gallery -> {
                // Change integer to be the count of how many qr codes have been saved.
                val fragment = SavedQrCodesFragment.newInstance(1)
                transaction.replace(R.id.fragment_container_view, fragment).commit()
                true
            }
            R.id.sign_out -> {
                // TODO: Implement sign out feature.
                auth = FirebaseAuth.getInstance();
                auth.signOut().also {
                    val intent = Intent(this, EmailPasswordActivity::class.java)
                    startActivity(intent)
                }
                Toast.makeText(this, "Sign out clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
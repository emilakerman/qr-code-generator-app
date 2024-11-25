package com.emilakerman.qrcodegenereator

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException

class QrRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance();
    private val client = OkHttpClient()

    // TODO: Add Toast For Success/Failure.
    fun uploadImage(bitmap: Bitmap)  {
        val bitMapUtils = BitMapUtils();
        // Convert Bitmap to byte array
        val byteArray = bitMapUtils.bitmapToByteArray(bitmap)

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
            .url("${keys.baseUrl}putQR")
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
    // Use this to see if user has reached the cap of 100 qr codes in the cloud.
    fun getImagesCount(): Int {
        val keys = ApiKeys()
        var length: Int = 0;
        val request = Request.Builder()
            .url("${keys.baseUrl}getQRCount")
            .addHeader("user", auth.currentUser?.uid.toString())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed with exception: ${e.message}")
                return
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val listLength: Int? = responseBody?.toIntOrNull()

                length = if (listLength != null) {
                    listLength;
                } else {
                    println("Failed to parse the list length as an integer.")
                    0
                }
            }
        })
        return length;
    }
    // This method fetches the QR codes for the logged in user.
    // The Request is sent to the Node.js server hosted on Vercel.
    // Node.js fetches the blobs/images from Vercel Blob Storage.
    suspend fun getImages(): List<String> {
        var urls: List<String> = listOf<String>();
        val keys = ApiKeys()
        val request = Request.Builder()
            .url("${keys.baseUrl}getQRCodes")
            .addHeader("user", auth.currentUser?.uid.toString())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed with exception: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val gson = Gson()
                    val listType = object : TypeToken<List<String>>() {}.type
                    val blobFileList: List<String> = gson.fromJson(responseBody, listType)
                    urls = blobFileList;
                } else {
                    println("Failed to parse the list length as an integer.")
                }
            }
        })
        delay(1000)
        return urls;
    }
    fun deleteQrCode(imageUrl: String) {
        val keys = ApiKeys()
        val request = Request.Builder()
            .url("${keys.localHost}deleteQR")
            .addHeader("user", auth.currentUser?.uid.toString())
            .addHeader("stuff", imageUrl)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed with exception: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    println("emil123" + responseBody)
                    println("Delete Succeeded emil123!!!")
                } else {
                    println("Delete Failed emil123!!!.")
                }
            }
        })
    }
}
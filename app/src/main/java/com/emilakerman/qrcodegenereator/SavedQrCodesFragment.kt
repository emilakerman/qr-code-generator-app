package com.emilakerman.qrcodegenereator

import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.emilakerman.qrcodegenereator.databinding.ActivityMainBinding
import com.emilakerman.qrcodegenereator.databinding.SavedQrCodesFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URL
import coil.load
import kotlinx.coroutines.delay


class SavedQrCodesFragment : Fragment(R.layout.saved_qr_codes_fragment) {
    private var _binding: SavedQrCodesFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth



    companion object {
        private const val passedCount = "passedCount"
        fun newInstance(exampleParam: Int): SavedQrCodesFragment {
            val fragment = SavedQrCodesFragment()
            val args = Bundle()
            args.putInt(passedCount, exampleParam)
            fragment.arguments = args
            return fragment
        }
    }

    private val client = OkHttpClient()

    private suspend fun getImages(): List<String> {
        var urls: List<String> = listOf<String>();
        auth = FirebaseAuth.getInstance();
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
                    println("Success!!: $responseBody")
                    val gson = Gson()
                    val listType = object : TypeToken<List<String>>() {}.type
                    val blobFileList: List<String> = gson.fromJson(responseBody, listType)
                    urls = blobFileList;
                    blobFileList.forEach {
                        println("emil url:" + it)
                    }
                } else {
                    println("Failed to parse the list length as an integer.")
                }
            }
        })
        delay(1000)
        return urls;
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SavedQrCodesFragmentBinding.inflate(inflater, container, false)
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container = binding.imageContainer
        val qrCodeCount = arguments?.getInt(passedCount)

        if (qrCodeCount != null) {
            lifecycleScope.launch {
                val images = getImages()
                // Dynamically add ImageViews with loaded images
                repeat(qrCodeCount) { index ->
                    val imageUrl = images.getOrNull(index)
                    if (imageUrl != null) {
                        val imageView = ImageView(requireContext()).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                            )
                            // Placeholder
                            setImageResource(R.drawable.placeholder)

                            // Load the images
                            load(imageUrl) {
                                placeholder(R.drawable.placeholder)
                                error(R.drawable.placeholder)
                            }
                            contentDescription = "$index"
                        }
                        container.addView(imageView)
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

}

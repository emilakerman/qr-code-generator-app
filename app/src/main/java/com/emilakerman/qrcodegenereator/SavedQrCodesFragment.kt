package com.emilakerman.qrcodegenereator

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.emilakerman.qrcodegenereator.databinding.SavedQrCodesFragmentBinding
import coil.load
import okhttp3.internal.wait

class SavedQrCodesFragment : Fragment(R.layout.saved_qr_codes_fragment) {
    private var _binding: SavedQrCodesFragmentBinding? = null
    private val binding get() = _binding!!
    private val imageHelper = ImageHelper();

    // Receives the data passed to the fragment.
    companion object {
        private const val PASSED_DATA = "passedData"
        fun newInstance(qrCodes: List<String>): SavedQrCodesFragment {
            val fragment = SavedQrCodesFragment()
            val args = Bundle()
            args.putStringArray(PASSED_DATA, qrCodes.toTypedArray())
            fragment.arguments = args
            return fragment
        }
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
        val progressBar = binding.progressBar
        val passedImages = arguments?.getStringArray(PASSED_DATA)
        progressBar.visibility = View.VISIBLE
        container.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed(
            {
                progressBar.visibility = View.GONE
                container.visibility = View.VISIBLE
            },
            1000,)
        if (passedImages != null) {
                // This adds ImageViews and Buttons dynamically with asynchronous images.
                repeat(passedImages.size) { index ->
                    val imageUrl = passedImages[index]
                    // Create a vertical LinearLayout to hold both ImageView and Button
                    val itemLayout = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }


                    // Create the ImageView for the image
                    val imageView = ImageView(requireContext()).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setImageResource(R.drawable.whiteprogress) // Placeholder
                        load(imageUrl) {
                            placeholder(R.drawable.whiteprogress)
                            error(R.drawable.whiteprogress)
                        }
                        contentDescription = "$index"
                    }

                    // Create the Button below the ImageView
                    val button = Button(requireContext()).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        // TODO: Change hardcoded text.
                        text = "Download"
                        setOnClickListener {
                            val imageUrl = passedImages[index].toString()
                            if (imageUrl.isEmpty()) {
                                Toast.makeText(requireContext(), "Invalid URL", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            } else {
                                imageHelper.saveImageFromUrl(requireContext(), imageUrl)
                            }
                        }

                    }
                    // TODO: This is probably not a good way to do this.
                    button.translationX = 200F;

                    // Add ImageView and Button to the item layout
                    itemLayout.addView(imageView)
                    itemLayout.addView(button)

                    // Add the item layout to the container
                    container.addView(itemLayout)
                }
            }
        }
    // Avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

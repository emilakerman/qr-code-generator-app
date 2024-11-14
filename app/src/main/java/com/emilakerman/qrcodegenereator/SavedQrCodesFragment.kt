package com.emilakerman.qrcodegenereator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.emilakerman.qrcodegenereator.databinding.SavedQrCodesFragmentBinding
import kotlinx.coroutines.launch
import coil.load


class SavedQrCodesFragment : Fragment(R.layout.saved_qr_codes_fragment) {
    private var _binding: SavedQrCodesFragmentBinding? = null
    private val binding get() = _binding!!
    private val qrRepository = QrRepository();

    // Receives the data passed to the fragment.
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
        val qrCodeCount = arguments?.getInt(passedCount)
        if (qrCodeCount != null) {
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                val images = qrRepository.getImages()
                progressBar.visibility = View.GONE
                // This adds ImageViews and Buttons dynamically with asynchronous images.
                repeat(qrCodeCount) { index ->
                    val imageUrl = images.getOrNull(index)
                    if (imageUrl != null) {
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
                            setImageResource(R.drawable.placeholder) // Placeholder
                            load(imageUrl) {
                                placeholder(R.drawable.placeholder)
                                error(R.drawable.placeholder)
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
                                // Handle button click here

                            }
                        }

                        // Add ImageView and Button to the item layout
                        itemLayout.addView(imageView)
                        itemLayout.addView(button)

                        // Add the item layout to the container
                        container.addView(itemLayout)
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

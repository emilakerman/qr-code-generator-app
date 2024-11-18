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
    private val qrRepository = QrRepository();

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
                    val downloadButton = Button(requireContext()).apply {
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
                    // Delete QR code image from vercel blob storage.
                    val deleteButton = Button(requireContext()).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        // TODO: Change hardcoded text.
                        text = "Delete"
                        setOnClickListener {
                            val imageUrl = passedImages[index].toString()
                            if (imageUrl.isEmpty()) {
                                Toast.makeText(requireContext(), "Invalid URL", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            } else {
                                qrRepository.deleteQrCode(imageUrl)
                                val updatedArray = passedImages.filterIndexed { _, image -> image != imageUrl }.toTypedArray()
                                container.removeAllViews()
                                // Repopulate the container with updated items
                                updatedArray.forEachIndexed { newIndex, newImageUrl ->
                                    val itemLayout = LinearLayout(requireContext()).apply {
                                        orientation = LinearLayout.VERTICAL
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                    }

                                    val imageView = ImageView(requireContext()).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        setImageResource(R.drawable.whiteprogress) // Placeholder
                                        load(newImageUrl) {
                                            placeholder(R.drawable.whiteprogress)
                                            error(R.drawable.whiteprogress)
                                        }
                                        contentDescription = "$newIndex"
                                    }

                                    val downloadButton = Button(requireContext()).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        // TODO: Fix hardcoded string.
                                        text = "Download"
                                        setOnClickListener {
                                            if (newImageUrl.isEmpty()) {
                                                Toast.makeText(requireContext(), "Invalid URL", Toast.LENGTH_SHORT).show()
                                                return@setOnClickListener
                                            } else {
                                                imageHelper.saveImageFromUrl(requireContext(), newImageUrl)
                                            }
                                        }
                                    }

                                    val deleteButton = Button(requireContext()).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        // TODO: Fix hardcoded string.
                                        text = "Delete"
                                        setOnClickListener {
                                            qrRepository.deleteQrCode(newImageUrl)
                                            val refreshedArray = updatedArray.filterIndexed { _, image -> image != newImageUrl }.toTypedArray()
                                            updateUI(refreshedArray)
                                        }
                                    }

                                    itemLayout.addView(imageView)
                                    itemLayout.addView(downloadButton)
                                    itemLayout.addView(deleteButton)

                                    container.addView(itemLayout)
                                }
                            }
                        }


                    }
                    // TODO: Fix, this is probably not a good way to do this.
                    downloadButton.translationX = 200F;
                    deleteButton.translationX = 300F;

                    // Add ImageView and Button to the item layout
                    itemLayout.addView(imageView)
                    itemLayout.addView(downloadButton)
                    itemLayout.addView(deleteButton)

                    // Add the item layout to the container
                    container.addView(itemLayout)
                }
            }
        }

    private fun updateUI(images: Array<String>) {
        val container = binding.imageContainer
        container.removeAllViews()

        images.forEachIndexed { index, imageUrl ->
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

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

            val downloadButton = Button(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                // TODO: Fix hardcoded string.
                text = "Download"
                setOnClickListener {
                    if (imageUrl.isEmpty()) {
                        Toast.makeText(requireContext(), "Invalid URL", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    } else {
                        imageHelper.saveImageFromUrl(requireContext(), imageUrl)
                    }
                }
            }

            val deleteButton = Button(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                // TODO: Fix hardcoded string.
                text = "Delete"
                setOnClickListener {
                    qrRepository.deleteQrCode(imageUrl)
                    val refreshedArray = images.filterIndexed { _, image -> image != imageUrl }.toTypedArray()
                    updateUI(refreshedArray)
                }
            }

            itemLayout.addView(imageView)
            itemLayout.addView(downloadButton)
            itemLayout.addView(deleteButton)

            container.addView(itemLayout)
        }
    }

    // Avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

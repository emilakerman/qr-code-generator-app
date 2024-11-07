package com.emilakerman.qrcodegenereator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.emilakerman.qrcodegenereator.databinding.ActivityMainBinding
import com.emilakerman.qrcodegenereator.databinding.SavedQrCodesFragmentBinding

class SavedQrCodesFragment : Fragment(R.layout.saved_qr_codes_fragment) {
    private var _binding: SavedQrCodesFragmentBinding? = null
    private val binding get() = _binding!!

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
        // change this integer to how many qr codes are saved
        repeat(5) { index ->
            val newImageView = ImageView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                // change image to correct qr code here
                setImageResource(R.drawable.placeholder)
                contentDescription = "$index"
            }
            container.addView(newImageView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

}

package com.emilakerman.qrcodegenereator

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emilakerman.qrcodegenereator.databinding.SavedQrCodesFragmentBinding

class SavedQrCodesFragment : Fragment(R.layout.saved_qr_codes_fragment) {
    private var _binding: SavedQrCodesFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var recycler : RecyclerView
    private lateinit var adapter : RecycleAdapter

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
        val progressBar = binding.progressBar
        val passedImages = arguments?.getStringArray(PASSED_DATA)
        progressBar.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed(
            {
                progressBar.visibility = View.GONE
                recycler = binding.recyclerView;
                adapter = RecycleAdapter(requireContext(), passedImages)
                recycler.layoutManager = LinearLayoutManager(requireContext())
                recycler.adapter = adapter
            },
            1000,
        )
    }

        // Avoid memory leaks
        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }

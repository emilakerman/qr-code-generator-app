package com.emilakerman.qrcodegenereator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.emilakerman.qrcodegenereator.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val qrRepository = QrRepository();
    private var images: List<String> = listOf("temp");

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance();
        // Sets some progress bars visibility to GONE, will set to VISIBLE later stages of app usage.
        binding.saveProgressbar.visibility = View.GONE
        binding.generateProgressbar.visibility = View.GONE
        binding.saveToCloudProgressbar.visibility = View.GONE

        // Initial fetch when app starts.
        fun fetchDataFromApi() {
            lifecycleScope.launch {
                try {
                    images = qrRepository.getImages();
                    println("Data: $images")
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }
        }
        fetchDataFromApi();

        val imageHelper = ImageHelper();
        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

        fun setupToolbar() {
            val toolbar: MaterialToolbar = binding.bottomAppBar;
            setSupportActionBar(binding.bottomAppBar)
            setSupportActionBar(toolbar)
            toolbar.title = ""
            toolbar.overflowIcon?.setTint(Color.WHITE)
        }
        setupToolbar()
        binding.inputField.addTextChangedListener {
            binding.clearButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
        // Zoom Toggle Feature for the QR Code ImageView.
        var toggleZoom = false
        binding.qrCodeImage.setOnClickListener {
            toggleZoom = !toggleZoom
            if (toggleZoom) {
                binding.qrCodeImage.animate()
                    .scaleX(binding.qrCodeImage.scaleX + 0.4f)
                    .scaleY(binding.qrCodeImage.scaleY + 0.4f)
                    .setDuration(300)
                    .start()
            } else {
                binding.qrCodeImage.animate()
                    .scaleX(binding.qrCodeImage.scaleX - 0.4f)
                    .scaleY(binding.qrCodeImage.scaleY - 0.4f)
                    .setDuration(300)
                    .start()
            }
        }
        // Generate Qr Code Feature.
        binding.generateButton.setOnClickListener {
            if (binding.inputField.text.toString() == "") {
                return@setOnClickListener;
            } else {
                binding.generateProgressbar.visibility = View.VISIBLE
                binding.generateButton.text = ""
                imageHelper.generateQrCode(binding).also {
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
        // Saves the QR Code Image to the device.
        binding.saveButton.setOnClickListener {
            if (binding.inputField.text.toString() == "") {
                return@setOnClickListener;
            } else {
                binding.saveProgressbar.visibility = View.VISIBLE
                binding.saveButton.visibility = View.GONE
                imageHelper.saveMediaToStorage(binding.qrCodeImage.drawToBitmap(), context = this).also {
                    // Adding a type of delay to simulate a loading state.
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.saveProgressbar.visibility = View.GONE
                            binding.saveButton.visibility = View.VISIBLE
                        },
                        1000
                    )
                }
            }
        }
        // Saves the Qr Code using the Node.js backend to Vercel Blob Storage.
        binding.saveToCloud.setOnClickListener {
            if (binding.inputField.text.toString() == "") {
                return@setOnClickListener
            } else if(images.lastIndex >= 100) {
                // Qr Code Cap Reached in Cloud. Cant Save More.
                Toast.makeText(this, "You have reached the max cap of 100 Qr Codes!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                binding.saveToCloudProgressbar.visibility = View.VISIBLE
                binding.saveToCloud.visibility = View.GONE

                lifecycleScope.launch {
                    // Upload image
                    qrRepository.uploadImage(binding.qrCodeImage.drawToBitmap())

                    delay(1000)

                    // Fetch updated images after upload completes
                    try {
                        images = qrRepository.getImages()
                    } catch (e: Exception) {
                        println("error fetching images after uploading a new one${e}")
                    }
                    binding.saveToCloudProgressbar.visibility = View.GONE
                    binding.saveToCloud.visibility = View.VISIBLE
                    binding.inputField.text?.clear();
                    binding.qrCodeImage.setImageResource(android.R.color.transparent);
                }
                Toast.makeText(this, "Qr Code Saved to Cloud!", Toast.LENGTH_SHORT).show()
            }
        }

        // Either clears the input field or pastes from clipboard.
        // Depending on if the field is empty or not.
        binding.clearButton.setOnClickListener {
            if (binding.inputField.text?.isEmpty() == true) {
                binding.inputField.setText(pasteFromClipboard(this).toString());
            } else {
                binding.inputField.text?.clear();
                binding.qrCodeImage.setImageResource(android.R.color.transparent);
                val typedValue = TypedValue()
                val theme = this.theme
                if (theme.resolveAttribute(android.R.attr.actionModePasteDrawable, typedValue, true)) {
                    val pasteDrawableResId = typedValue.resourceId
                    binding.clearButton.setImageResource(pasteDrawableResId)
                }
            }
        }
    }
    // Pastes whatever the user has in their clipboard to the edit test field.
     private fun pasteFromClipboard(context: Context): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip() && clipboard.primaryClip != null) {
            val clipData: ClipData = clipboard.primaryClip!!
            val item = clipData.getItemAt(0)
            return item.text?.toString()
        }
        return null
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }
    // Navigation.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val transaction = supportFragmentManager.beginTransaction()
        return when (item.itemId) {
            R.id.home -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                lifecycleScope.launch {
                    try {
                        images = qrRepository.getImages();
                        println("Data: $images")
                        delay(1000)
                    } catch (e: Exception) {
                        println("Error: ${e.message}")
                    }
                }
                if (fragment != null) {
                    transaction.remove(fragment).commit()
                }
                true
            }
            // Opens the fragment that displays a list of Qr Codes saved in the cloud.
            R.id.gallery -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                // Disables the Gallery Menu Icon when either of the three is true.
                if (images.isEmpty() || images.contains("temp") || fragment is SavedQrCodesFragment) {
                    return false
                } else {
                    val fragmentToCommit = SavedQrCodesFragment.newInstance(images)
                    transaction.replace(R.id.fragment_container_view, fragmentToCommit).commit()
                    true
                }
            }
            // Signs out user and redirects to start screen.
            R.id.sign_out -> {
                auth = FirebaseAuth.getInstance();
                auth.signOut().also {
                    val intent = Intent(this, EmailPasswordActivity::class.java)
                    startActivity(intent)
                }
                Toast.makeText(this, "Signed out!", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
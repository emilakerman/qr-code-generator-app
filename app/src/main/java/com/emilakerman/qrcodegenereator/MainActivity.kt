package com.emilakerman.qrcodegenereator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.emilakerman.qrcodegenereator.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val qrRepository = QrRepository();
    private lateinit var images: List<String>;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.saveProgressbar.visibility = View.GONE
        binding.generateProgressbar.visibility = View.GONE
        binding.saveToCloudProgressbar.visibility = View.GONE
        auth = FirebaseAuth.getInstance();

        lifecycleScope.launch {
            images = qrRepository.getImages();
        }

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
        }
        setupToolbar()
        binding.inputField.addTextChangedListener {
            binding.clearButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }

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
        binding.saveToCloud.setOnClickListener {
            if (binding.inputField.text.toString() == "") {
                return@setOnClickListener;
            } else {
                // Saves QR Code to cloud/vercel
                binding.saveToCloudProgressbar.visibility = View.VISIBLE
                binding.saveToCloud.visibility = View.GONE
                qrRepository.uploadImage(binding.qrCodeImage.drawToBitmap()).also {
                    // Adding a type of delay to simulate a loading state.
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.saveToCloudProgressbar.visibility = View.GONE
                            binding.saveToCloud.visibility = View.VISIBLE
                        },
                        1000
                    )
                }
            }
            lifecycleScope.launch {
                images = qrRepository.getImages();
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
                val fragment = SavedQrCodesFragment.newInstance(images)
                transaction.replace(R.id.fragment_container_view, fragment).commit()
                true
            }
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
package com.emilakerman.qrcodegenereator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emilakerman.qrcodegenereator.databinding.ActivityEmailPasswordBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class EmailPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityEmailPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
            binding.qrHeaderImage.visibility = View.VISIBLE
        }
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityEmailPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance();
        binding.qrHeaderImage.setImageResource(R.drawable.image);
        // Hides the QR Header Code when typing in fields. Helps smaller screens.
        binding.editTextTextEmailAddress.setOnFocusChangeListener { _, b -> binding.qrHeaderImage.visibility = View.GONE }
        binding.editTextTextPassword.setOnFocusChangeListener { _, b -> binding.qrHeaderImage.visibility = View.GONE }

        binding.signInButton.setOnClickListener {
            it.hideKeyboard()
            if (fieldsEmpty()) {
                return@setOnClickListener;
            } else {
                signInUser(binding.editTextTextEmailAddress.text.toString(), binding.editTextTextPassword.text.toString());
            }
        }
        binding.signUpButton.setOnClickListener {
            it.hideKeyboard()
            if (fieldsEmpty()) {
                return@setOnClickListener;
            } else {
                createUser(binding.editTextTextEmailAddress.text.toString(), binding.editTextTextPassword.text.toString());
            }
        }
    }
    // Reusable function that checks if either password or email fields are empty.
    private fun fieldsEmpty(): Boolean {
        return binding.editTextTextEmailAddress.text.toString().isEmpty() || binding.editTextTextPassword.text.toString().isEmpty()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and redirects user to MainActivity.
        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        baseContext,
                        "${getString(R.string.sign_in_failed)} ${task.exception?.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
    }
    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    binding.passwordTip.visibility = View.VISIBLE;
                    Toast.makeText(
                        baseContext,
                        "${getString(R.string.sign_up_failed)} ${task.exception?.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
    }
}
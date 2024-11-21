package com.emilakerman.qrcodegenereator

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emilakerman.qrcodegenereator.databinding.ActivityEmailPasswordBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class EmailPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityEmailPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityEmailPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance();
        binding.qrHeaderImage.setImageResource(R.drawable.image);
        binding.signInButton.setOnClickListener {
            if (fieldsEmpty()) {
                return@setOnClickListener;
            } else {
                signInUser(binding.editTextTextEmailAddress.text.toString(), binding.editTextTextPassword.text.toString());
            }
        }
        binding.signUpButton.setOnClickListener {
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
        // Check if user is signed in (non-null) and redirects user to mainactivity.
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
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
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
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}
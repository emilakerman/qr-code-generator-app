package com.emilakerman.qrcodegenereator

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emilakerman.qrcodegenereator.databinding.ActivityEmailPasswordBinding
import com.emilakerman.qrcodegenereator.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.initialize

class EmailPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityEmailPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityEmailPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance();
        binding.signInButton.setOnClickListener {
            if (binding.editTextTextEmailAddress.text.toString() == "") {
                return@setOnClickListener;
            } else {
            signInUser(binding.editTextTextEmailAddress.text.toString(),
                binding.editTextTextPassword.text.toString());
            }
        }
        binding.signUpButton.setOnClickListener {
            if (binding.editTextTextEmailAddress.text.toString() == "") {
                return@setOnClickListener;
            } else {
                createUser(
                    binding.editTextTextEmailAddress.text.toString(),
                    binding.editTextTextPassword.text.toString()
                );
            }
        }
    }

        public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
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
                    /*updateUI(null)*/
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
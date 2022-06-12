package com.tristate.gasper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.tristate.gasper.databinding.ActivityResetPasswdBinding

class ResetPasswdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswdBinding

    private lateinit var sendEmail: EditText
    private lateinit var resetButton: Button

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_passwd)
        binding = ActivityResetPasswdBinding.bind(findViewById(R.id.reset_passwd_layout))

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Reset password"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sendEmail = binding.sendEmail
        resetButton = binding.resetButton

        firebaseAuth = FirebaseAuth.getInstance()

        resetButton.setOnClickListener {
            val email: String = sendEmail.text.toString()

            if (email.equals("")) {
                Toast.makeText(this@ResetPasswdActivity, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this@ResetPasswdActivity, "Please check your email", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@ResetPasswdActivity, SignInActivity::class.java))
                    } else {
                        val error: String = it.exception!!.message!!
                        Toast.makeText(this@ResetPasswdActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
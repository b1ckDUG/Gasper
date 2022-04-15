package com.tristate.gasper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.rengwuxian.materialedittext.MaterialEditText
import com.tristate.gasper.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    private lateinit var email: MaterialEditText
    private lateinit var password: MaterialEditText
    private lateinit var signIn: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        binding = ActivitySignInBinding.bind(findViewById(R.id.login_layout))

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Sign in"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        email = binding.loginEmail
        password = binding.loginPwd
        signIn = binding.signIn

        auth = FirebaseAuth.getInstance()

        signIn.setOnClickListener {
            val emailTxt = email.text.toString()
            val passwordTxt = password.text.toString()

            when {
                TextUtils.isEmpty(emailTxt) || TextUtils.isEmpty(passwordTxt) -> {
                    Toast.makeText(this, "All inputs are required", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    auth.signInWithEmailAndPassword(emailTxt, passwordTxt)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Authentication failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }

}
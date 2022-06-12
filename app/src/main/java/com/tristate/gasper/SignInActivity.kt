package com.tristate.gasper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
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

    private lateinit var forgetPasswd: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        binding = ActivitySignInBinding.bind(findViewById(R.id.sign_in_layout))

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Sign in"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        email = binding.email
        password = binding.password
        signIn = binding.signIn
        forgetPasswd = binding.forgetPasswd

        auth = FirebaseAuth.getInstance()

        forgetPasswd.setOnClickListener {
            startActivity(Intent(this@SignInActivity, ResetPasswdActivity::class.java))
        }

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
                                if (auth.currentUser!!.isEmailVerified) {
                                    intent = Intent(this, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this@SignInActivity, "Please verify your email", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Authentication failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }

}
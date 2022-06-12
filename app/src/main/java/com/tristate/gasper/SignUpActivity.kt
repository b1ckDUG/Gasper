package com.tristate.gasper

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import com.tristate.gasper.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var username: MaterialEditText
    private lateinit var email: MaterialEditText
    private lateinit var password: MaterialEditText
    private lateinit var rptPassword: MaterialEditText
    private lateinit var signUp: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        binding = ActivitySignUpBinding.bind(findViewById(R.id.sign_up_layout))

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Sign up"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        username = binding.username
        email = binding.email
        password = binding.password
        rptPassword = binding.rptPassword
        signUp = binding.signUp

        auth = FirebaseAuth.getInstance()

        signUp.setOnClickListener {
            val usernameTxt = username.text.toString()
            val emailTxt = email.text.toString()
            val passwordTxt = password.text.toString()
            val rptPwdTxt = rptPassword.text.toString()

            val pwdPattern =
                "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[.#?!@\$%^&*-]).{8,}\$".toRegex()

            when {
                TextUtils.isEmpty(usernameTxt) || TextUtils.isEmpty(emailTxt) || TextUtils.isEmpty(passwordTxt) || TextUtils.isEmpty(rptPwdTxt) -> {
                    Toast.makeText(this, "All inputs are required", Toast.LENGTH_SHORT).show()
                }
                !pwdPattern.matches(passwordTxt) -> {
                    Toast.makeText(
                        this,
                        "Password must contain at least 8 characters, 1 number, 1 lowercase letter, 1 uppercase letter and 1 special character",
                        Toast.LENGTH_LONG
                    ).show()
                }
                !TextUtils.equals(passwordTxt, rptPwdTxt) -> {
                    Toast.makeText(this, "Passwords does not match", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    signUp(usernameTxt, emailTxt, passwordTxt)
                }
            }
        }
    }

    private fun signUp(username: String, email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    val firebaseUser: FirebaseUser = auth.currentUser!!
                    val userid: String = firebaseUser.uid

                    firebaseUser.sendEmailVerification().addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@SignUpActivity,
                                "Please check your email for verification",
                                Toast.LENGTH_SHORT
                            ).show()
                            this.username.setText("")
                            this.email.setText("")
                            this.password.setText("")
                            this.rptPassword.setText("")
                        } else {
                            Toast.makeText(this@SignUpActivity, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)

                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap["id"] = userid
                    hashMap["username"] = username
                    hashMap["imageURI"] = "default"
                    hashMap["status"] = "offline"
                    reference.setValue(hashMap)
                } else -> {
                    Toast.makeText(
                        this,
                        "You cannot register with this email or password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
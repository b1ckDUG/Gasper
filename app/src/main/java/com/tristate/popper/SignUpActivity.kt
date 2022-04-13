package com.tristate.popper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import com.tristate.popper.databinding.ActivitySignUpBinding

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
        binding = ActivitySignUpBinding.bind(findViewById(R.id.reg_layout))

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Sign up"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        username = binding.regUname
        email = binding.regEmail
        password = binding.regPwd
        rptPassword = binding.rptPwd
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

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)

                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap["id"] = userid
                    hashMap["username"] = username
                    hashMap["imageURL"] = "default"

                    reference.setValue(hashMap).addOnCompleteListener { task ->
                        when {
                            task.isSuccessful -> {
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            else -> {
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
        }
    }
}
package com.tristate.popper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.tristate.popper.databinding.ActivityStartBinding


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private lateinit var signUp: Button
    private lateinit var signIn: Button

    //private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        binding = ActivityStartBinding.bind(findViewById(R.id.start_layout))

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        signIn = binding.startSignIn
        signUp = binding.startSignUp

        signIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
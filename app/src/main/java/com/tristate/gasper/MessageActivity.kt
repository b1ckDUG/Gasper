package com.tristate.gasper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.tristate.gasper.adapter.MessageAdapter
import com.tristate.gasper.databinding.ActivityMessageBinding
import com.tristate.gasper.model.GasperMessage
import com.tristate.gasper.model.User
import de.hdodenhof.circleimageview.CircleImageView

class MessageActivity : AppCompatActivity() {

    private lateinit var profileImage: CircleImageView
    private lateinit var username: TextView
    private lateinit var sendButton: ImageButton
    private lateinit var photoButton: ImageButton
    private lateinit var msgText: EditText

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var mMsgs: ArrayList<GasperMessage>

    private lateinit var recyclerView: RecyclerView

    private lateinit var binding: ActivityMessageBinding

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var reference: DatabaseReference

    private lateinit var _intent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        binding = ActivityMessageBinding.bind(findViewById(R.id.message_layout))

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager= LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        profileImage = binding.profileImage
        username = binding.username
        sendButton = binding.sendButton
        photoButton = binding.photoButton
        msgText = binding.msgText

        _intent = intent
        val userid: String = _intent.getStringExtra("userid").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        //sendButton.set

        msgText.addTextChangedListener(MyButtonObserver(sendButton))

        sendButton.setOnClickListener{
            val message: String = msgText.text.toString()
            if (message != "") sendMessage(firebaseUser.uid, userid, message)
            msgText.setText("")
        }

        photoButton.setOnClickListener{

        }

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User = snapshot.getValue(User::class.java)!!
                username.text = user.username
                if (user.imageURI.equals("default")) {
                    profileImage.setImageResource(R.drawable.ic_account_circle_black_36dp)
                } else {
                    Glide.with(this@MessageActivity).load(user.imageURI).into(profileImage)
                }

                readMessage(firebaseUser.uid, userid)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun sendMessage(sender: String, receiver: String, text: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["sender"] = sender
        hashMap["receiver"] = receiver
        hashMap["text"] = text
        hashMap["photoURI"] = ""

        reference.child("Messages").push().setValue(hashMap)
    }

    fun readMessage(myid: String, userid: String) {
        mMsgs = ArrayList()

        reference = FirebaseDatabase.getInstance().getReference("Messages")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mMsgs.clear()
                for (snapshot: DataSnapshot in dataSnapshot.children) {
                    val msg: GasperMessage = snapshot.getValue(GasperMessage::class.java)!!
                    if (msg.receiver.equals(myid) && msg.sender.equals(userid) ||
                        msg.receiver.equals(userid) && msg.sender.equals(myid)) {
                        mMsgs.add(msg)
                    }

                    messageAdapter = MessageAdapter(this@MessageActivity, mMsgs)
                    recyclerView.adapter = messageAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    class MyButtonObserver(private val button: ImageButton) : TextWatcher {
        override fun onTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
            if (charSequence.toString().trim().isNotEmpty()) {
                button.isEnabled = true
                button.setBackgroundResource(R.drawable.outline_send_24)
            } else {
                button.isEnabled = false
                button.setBackgroundResource(R.drawable.outline_send_gray_24)
            }
        }

        override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
    }
}
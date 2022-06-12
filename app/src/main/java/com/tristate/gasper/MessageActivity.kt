package com.tristate.gasper

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.tristate.gasper.adapter.MessageAdapter
import com.tristate.gasper.databinding.ActivityMessageBinding
import com.tristate.gasper.fragment.ProfileFragment
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

    private lateinit var storageReference: StorageReference
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var reference: DatabaseReference

    private lateinit var _intent: Intent
    private lateinit var userid: String

    private lateinit var imageUri: Uri
    private lateinit var uploadTask: UploadTask

    private lateinit var seenListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        binding = ActivityMessageBinding.bind(findViewById(R.id.message_layout))

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
            startActivity(Intent(this@MessageActivity, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

        recyclerView = binding.msgRecycler
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
        userid = _intent.getStringExtra("userid").toString()

        storageReference = FirebaseStorage.getInstance().getReference("uploads")

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        msgText.addTextChangedListener(MyButtonObserver(sendButton))

        sendButton.setOnClickListener{
            val message: String = msgText.text.toString()
            if (message != "") sendMessage(firebaseUser.uid, userid, message, "")
            msgText.setText("")
        }

        photoButton.setOnClickListener{
            openImage()
        }

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                username.text = user!!.username
                if (user.imageURI.equals("default")) {
                    profileImage.setImageResource(R.drawable.ic_account_circle_black_36dp)
                } else {
                    Glide.with(applicationContext).load(user.imageURI).into(profileImage)
                }

                readMessage(firebaseUser.uid, userid)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        seenMessage(userid)
    }

    private fun seenMessage(userid: String) {
        reference = FirebaseDatabase.getInstance().getReference("Messages")
        seenListener = reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val msg: GasperMessage = dataSnapshot.getValue(GasperMessage::class.java)!!
                    if (msg.receiver.equals(firebaseUser.uid) && msg.sender.equals(userid)) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["seen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun sendMessage(sender: String, receiver: String, text: String, photoURI: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference

        val hashMap = HashMap<String, Any?>()
        hashMap["sender"] = sender
        hashMap["receiver"] = receiver
        hashMap["text"] = text
        hashMap["photoURI"] = photoURI
        hashMap["seen"] = false

        reference.child("Messages").push().setValue(hashMap)

        var chatRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("ChatItem")
            .child(firebaseUser.uid).child(userid)

        chatRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userid)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        chatRef = FirebaseDatabase.getInstance().getReference("ChatItem")
            .child(userid).child(firebaseUser.uid)

        chatRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(firebaseUser.uid)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
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
            }
        })
    }

    private fun status(status: String) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status

        reference.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        status("online")
    }

    override fun onPause() {
        super.onPause()
        reference.removeEventListener(seenListener)
        status("offline")
    }

    private fun openImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String {
        val contentResolver = applicationContext?.contentResolver
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver!!.getType(uri)).toString()
    }

    private fun uploadImage() {
        val progressDialog = ProgressDialog(this@MessageActivity)
        progressDialog.setMessage("Uploading")
        progressDialog.show()
        val fileReference = storageReference.child(
            System.currentTimeMillis()
                .toString() + "." + getFileExtension(imageUri)
        )
        uploadTask = fileReference.putFile(imageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            fileReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val mUri = downloadUri.toString()
                sendMessage(firebaseUser.uid, userid, "", "" + mUri)
                progressDialog.dismiss()
            } else {
                Toast.makeText(this@MessageActivity, "Failed!", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this@MessageActivity, e.message, Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.data != null) {
            imageUri = data.data!!

            uploadImage()
        }
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

    companion object {
        const val IMAGE_REQUEST = 1
    }
}
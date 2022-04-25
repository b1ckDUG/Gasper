package com.tristate.gasper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
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
import com.tristate.gasper.adapter.MessageAdapter
import com.tristate.gasper.databinding.ActivityMessageBinding
import com.tristate.gasper.model.GasperMessage
import com.tristate.gasper.model.User
import de.hdodenhof.circleimageview.CircleImageView
import java.io.InputStream

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
    private lateinit var userid: String

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
        userid = _intent.getStringExtra("userid").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        msgText.addTextChangedListener(MyButtonObserver(sendButton))

        sendButton.setOnClickListener{
            val message: String = msgText.text.toString()
            if (message != "") sendMessage(firebaseUser.uid, userid, message, "")
            msgText.setText("")
        }

        photoButton.setOnClickListener{
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@MessageActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE_PERMISSION)
            } else {
                selectImage()
            }
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


    private fun sendMessage(sender: String, receiver: String, text: String, photoURI: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["sender"] = sender
        hashMap["receiver"] = receiver
        hashMap["text"] = text
        hashMap["photoURI"] = photoURI

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

    fun selectImage() {
        val intent: Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedPhotoURI: Uri = data.data!!
                /*try {
                    val inputStream: InputStream = contentResolver.openInputStream(selectedPhotoURI)!!
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    //setimagebitmap
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }*/
                sendMessage(firebaseUser.uid, userid, "", selectedPhotoURI.toString())
            }
        }
    }

    @SuppressLint("Recycle")
    fun getPathFromURI(content: Uri): String {
        val filePath: String
        val cursor: Cursor? = contentResolver.query(content, null, null, null, null)

        if (cursor == null) {
            filePath = content.path.toString()
        } else {
            cursor.moveToFirst()
            val index: Int = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }

        return filePath
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
        const val REQUEST_CODE_STORAGE_PERMISSION = 1
        const val REQUEST_CODE_SELECT_IMAGE = 2
    }
}
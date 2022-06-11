package com.tristate.gasper.fragment

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.*
import com.tristate.gasper.R
import com.tristate.gasper.databinding.FragmentProfileBinding
import com.tristate.gasper.model.User
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment: Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var imageProfile: CircleImageView
    private lateinit var username: TextView

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var reference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var uploadTask: UploadTask

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        imageProfile = binding.profileImage
        username = binding.username

        storageReference = FirebaseStorage.getInstance().getReference("uploads")

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        reference =FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User = dataSnapshot.getValue(User::class.java)!!
                username.text = user.username
                if (user.imageURI == "default") {
                    imageProfile.setImageResource(R.drawable.ic_account_circle_black_36dp)
                } else {
                    context?.let { Glide.with(it).load(user.imageURI).into(imageProfile) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        imageProfile.setOnClickListener {
            openImage()
        }

        return binding.root
    }

    private fun openImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String {
        val contentResolver = context?.contentResolver
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver!!.getType(uri)).toString()
    }

    private fun uploadImage() {
        val progressDialog = ProgressDialog(context)
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
                reference =
                    FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)
                val map = HashMap<String, Any>()
                map["imageURI"] = "" + mUri
                reference.updateChildren(map)
                progressDialog.dismiss()
            } else {
                Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
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

    companion object {
        var IMAGE_REQUEST: Int = 1
    }
}
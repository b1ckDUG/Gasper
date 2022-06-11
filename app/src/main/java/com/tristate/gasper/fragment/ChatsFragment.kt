package com.tristate.gasper.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.tristate.gasper.adapter.UserAdapter
import com.tristate.gasper.databinding.FragmentChatsBinding
import com.tristate.gasper.model.GasperMessage
import com.tristate.gasper.model.User

class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var mUsers: ArrayList<User>

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var reference: DatabaseReference

    private lateinit var usersList: ArrayList<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)

        recyclerView = binding.recycleView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        usersList = ArrayList()

        reference = FirebaseDatabase.getInstance().getReference("Messages")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usersList.clear()

                for (snapshot in dataSnapshot.children) {
                    val msg: GasperMessage = snapshot.getValue(GasperMessage::class.java)!!
                    if (msg.sender!! == firebaseUser.uid) {
                        var isAdded = false
                        for (id in usersList) {
                            if (msg.receiver == id) {
                                isAdded = true
                                break
                            }
                        }
                        if (!isAdded) usersList.add(msg.receiver!!)
                    }
                    if (msg.receiver!! == firebaseUser.uid) {
                        var isAdded = false
                        for (id in usersList) {
                            if (msg.receiver == id) {
                                isAdded = true
                                break
                            }
                        }
                        if (!isAdded) usersList.add(msg.sender!!)
                    }
                }

                readChats()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        return binding.root
    }

    private fun readChats() {
        mUsers = ArrayList()

        reference = FirebaseDatabase.getInstance().getReference("Users")

        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers.clear()

                for (snapshot in dataSnapshot.children) {
                    val user: User = snapshot.getValue(User::class.java)!!

                    for (id in usersList) {
                        if (user.id == id) {
                            mUsers.add(user)
                        }
                    }
                }

                userAdapter = UserAdapter(context, mUsers, false)
                recyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}
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
import com.google.firebase.messaging.FirebaseMessaging
import com.tristate.gasper.adapter.UserAdapter
import com.tristate.gasper.databinding.FragmentChatsBinding
import com.tristate.gasper.model.ChatItem
import com.tristate.gasper.model.User

class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var mUsers: ArrayList<User>

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var reference: DatabaseReference

    private lateinit var usersList: ArrayList<ChatItem>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)

        recyclerView = binding.chatsRecycler
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        usersList = ArrayList()

        reference = FirebaseDatabase.getInstance().getReference("ChatItem").child(firebaseUser.uid)
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (dataSnapshot in snapshot.children) {
                    val chatItem = dataSnapshot.getValue(ChatItem::class.java)
                    usersList.add(chatItem!!)
                }

                chatItem()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        return binding.root
    }

    private fun chatItem() {
        mUsers = ArrayList()
        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mUsers.clear()
                for (dataSnapshot in snapshot.children) {
                    val user: User = dataSnapshot.getValue(User::class.java)!!
                    for (chatItem: ChatItem in usersList) {
                        if (user.id.equals(chatItem.id)) {
                            mUsers.add(user)
                        }
                    }
                }
                userAdapter = UserAdapter(context, mUsers, true)
                recyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
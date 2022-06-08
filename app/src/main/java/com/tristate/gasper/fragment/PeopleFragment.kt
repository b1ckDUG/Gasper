package com.tristate.gasper.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tristate.gasper.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.tristate.gasper.R
import com.tristate.gasper.adapter.UserAdapter
import com.tristate.gasper.databinding.FragmentPeopleBinding

class PeopleFragment : Fragment() {

    private lateinit var binding: FragmentPeopleBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var mUsers: ArrayList<User>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeopleBinding.inflate(inflater, container, false)
        val view: View = binding.root

        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        mUsers = ArrayList()

        readUsers()

        return view
    }

    private fun readUsers() {
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers.clear()
                for (snapshot in dataSnapshot.children) {
                    val user: User = snapshot.getValue(User::class.java)!!

                    if (!user.id.equals(firebaseUser.uid)) {
                        mUsers.add(user)
                    }
                }

                userAdapter = UserAdapter(context!!, mUsers)
                recyclerView.adapter = userAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}
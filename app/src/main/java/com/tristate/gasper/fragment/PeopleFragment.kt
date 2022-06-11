package com.tristate.gasper.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tristate.gasper.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.tristate.gasper.adapter.UserAdapter
import com.tristate.gasper.databinding.FragmentPeopleBinding

class PeopleFragment : Fragment() {

    private lateinit var binding: FragmentPeopleBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var mUsers: ArrayList<User>
    private lateinit var searchUsers: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeopleBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        mUsers = ArrayList()

        readUsers()

        searchUsers = binding.searchUsers
        searchUsers.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        return binding.root
    }

    private fun searchUsers(str: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val query: Query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
            .startAt(str)
            .endAt(str + "\uf8ff")

        query.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mUsers.clear()
                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)

                    if (user!!.id != firebaseUser!!.uid) {
                        mUsers.add(user)
                    }
                }

                userAdapter = UserAdapter(context!!, mUsers, false)
                recyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun readUsers() {
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (searchUsers.text.toString() == "") {
                    mUsers.clear()
                    for (snapshot in dataSnapshot.children) {
                        val user: User = snapshot.getValue(User::class.java)!!

                        if (!user.id.equals(firebaseUser.uid)) {
                            mUsers.add(user)
                        }
                    }
                    userAdapter = UserAdapter(context, mUsers, false) }
                    recyclerView.adapter = userAdapter
                }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}
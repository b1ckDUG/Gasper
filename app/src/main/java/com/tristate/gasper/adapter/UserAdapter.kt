package com.tristate.gasper.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tristate.gasper.MessageActivity
import com.tristate.gasper.model.User
import com.tristate.gasper.R

class UserAdapter(
    private val mContext: Context?,
    private val mUsers: ArrayList<User>,
    private val isChat: Boolean
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView = itemView.findViewById(R.id.username)
        var profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val imgOn: ImageView = itemView.findViewById(R.id.img_on)
        val imgOff: ImageView = itemView.findViewById(R.id.img_off)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: User = mUsers[position]
        holder.username.text = user.username
        if (user.imageURI.equals("default")) {
            holder.profileImage.setImageResource(R.drawable.ic_account_circle_black_36dp)
        } else {
            Glide.with(mContext!!).load(user.imageURI).into(holder.profileImage)
        }

        if (isChat) {
            if (user.status == "online") {
                holder.imgOn.visibility = View.VISIBLE
                holder.imgOff.visibility = View.GONE
            } else {
                holder.imgOn.visibility = View.GONE
                holder.imgOff.visibility = View.VISIBLE
            }
        } else {
            holder.imgOn.visibility = View.GONE
            holder.imgOff.visibility = View.GONE
        }

        holder.itemView.setOnClickListener{
            val intent = Intent(mContext, MessageActivity::class.java)
            intent.putExtra("userid", user.id)
            mContext!!.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }
}
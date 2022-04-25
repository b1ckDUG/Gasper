package com.tristate.gasper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tristate.gasper.R
import com.tristate.gasper.model.GasperMessage

const val MSG_TYPE_RECEIVED = 0
const val MSG_TYPE_SENT = 1

class MessageAdapter(private val mContext: Context,
                     private val mMsgs: ArrayList<GasperMessage>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private lateinit var firebaseUser: FirebaseUser

    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var message: TextView = itemView.findViewById(R.id.messageTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == MSG_TYPE_SENT) {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.sent_message_item, parent, false)
            ViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.received_message_item, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg: GasperMessage = mMsgs[position]

        holder.message.text = msg.message

        /*if (imageURI == "default") {
            holder.profileImage.setImageResource(R.mipmap.ic_launcher)
        } else {
            Glide.with(mContext).load(imageURI).into(holder.profileImage)
        }*/
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        return if (mMsgs[position].sender.equals(firebaseUser.uid)) {
            MSG_TYPE_SENT
        } else {
            MSG_TYPE_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return mMsgs.size
    }
}
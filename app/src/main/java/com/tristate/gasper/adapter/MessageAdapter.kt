package com.tristate.gasper.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tristate.gasper.R
import com.tristate.gasper.model.GasperMessage
import java.security.KeyStore

class MessageAdapter(private val mContext: Context,
                     private val mMsgs: ArrayList<GasperMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var firebaseUser: FirebaseUser

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textItem: TextView = itemView.findViewById(R.id.message_view)
        var seenText: TextView = itemView.findViewById(R.id.seen_text)
    }

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var photoItem: ImageView = itemView.findViewById(R.id.message_image)
        var seenText: TextView = itemView.findViewById(R.id.seen_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TXT_TYPE_SENT -> {
                val view: View =
                    LayoutInflater.from(mContext).inflate(R.layout.sent_message_item, parent, false)
                TextViewHolder(view)
            }
            TXT_TYPE_RECEIVED -> {
                val view: View =
                    LayoutInflater.from(mContext).inflate(R.layout.received_message_item, parent, false)
                TextViewHolder(view)
            }
            PHOTO_TYPE_SENT -> {
                val view: View =
                    LayoutInflater.from(mContext).inflate(R.layout.sent_photo_item, parent, false)
                PhotoViewHolder(view)
            }
            else -> {
                val view: View =
                    LayoutInflater.from(mContext).inflate(R.layout.received_photo_item, parent, false)
                PhotoViewHolder(view)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg: GasperMessage = mMsgs[position]
        if (!msg.text.equals("")) {
            (holder as TextViewHolder).textItem.text = msg.text
        } else {
            Glide.with((holder as PhotoViewHolder).photoItem.context).load(msg.photoURI).into(holder.photoItem)
        }

        if (position == mMsgs.size - 1) {
            if (msg.seen) {
                if (!msg.text.equals("")) {
                    (holder as TextViewHolder).seenText.text = "Seen"
                    holder.seenText.visibility = View.VISIBLE
                }
                else {
                    (holder as PhotoViewHolder).seenText.text = "Seen"
                    holder.seenText.visibility = View.VISIBLE
                }
            } else {
                if (!msg.text.equals("")) {
                    (holder as TextViewHolder).seenText.text = "Delivered"
                    holder.seenText.visibility = View.VISIBLE
                } else {
                    (holder as PhotoViewHolder).seenText.text = "Delivered"
                    holder.seenText.visibility = View.VISIBLE
                }
            }
        } else {
            if (!msg.text.equals("")) (holder as TextViewHolder).seenText.visibility = View.GONE
            else (holder as PhotoViewHolder).seenText.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        return if (mMsgs[position].sender.equals(firebaseUser.uid) && !mMsgs[position].text.equals("")) {
            TXT_TYPE_SENT
        } else if (!mMsgs[position].sender.equals(firebaseUser.uid) && !mMsgs[position].text.equals("")) {
            TXT_TYPE_RECEIVED
        } else if (mMsgs[position].sender.equals(firebaseUser.uid) && mMsgs[position].text.equals("") ) {
            PHOTO_TYPE_SENT
        } else {
            PHOTO_TYPE_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return mMsgs.size
    }

    companion object {
        const val TXT_TYPE_RECEIVED = 0
        const val TXT_TYPE_SENT = 1
        const val PHOTO_TYPE_RECEIVED = 2
        const val PHOTO_TYPE_SENT = 3
    }
}
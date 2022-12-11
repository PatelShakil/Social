package com.bcgroup.social_media.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.ReceiverSampleMessageBinding
import com.bcgroup.databinding.SenderSampleMessageBinding
import com.bcgroup.social_media.models.ChatModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>{
    var msglist:List<ChatModel>
    var senderid:String
    var context : Context

    constructor(msglist: List<ChatModel>, senderid: String,context: Context) {
        this.msglist = msglist
        this.senderid = senderid
        this.context = context
    }


    final var VIEW_TYPE_SENT = 1
    final var VIEW_TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        if (msglist[position].senderid == senderid)
            return VIEW_TYPE_SENT
        else
            return VIEW_TYPE_RECEIVED
    }
    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding:SenderSampleMessageBinding
        init {
            binding = SenderSampleMessageBinding.bind(itemView)
        }
        fun setData(msg:ChatModel,context : Context) {
            if (msg.message == "false") {
                binding.senderTv.visibility = View.GONE
                binding.senderPostCard.visibility = View.VISIBLE
                FirebaseDatabase.getInstance().reference
                    .child("social_media")
                    .child("posts")
                    .child(msg.post)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var postImg = snapshot.child("post_url").getValue().toString()
                                binding.senderPostIv.setImageBitmap(Constants().decodeImage(postImg))
                                FirebaseFirestore.getInstance()
                                    .collection(Constants().KEY_COLLECTION_USERS)
                                    .document(snapshot.child("post_author").getValue().toString())
                                    .addSnapshotListener { value, error ->
                                        if (error != null)
                                            return@addSnapshotListener
                                        if (value != null) {
                                            if (value.exists()) {
                                                if (context != null) {
                                                    Glide.with(context)
                                                        .load(
                                                            value.getString("profile_pic")
                                                                .toString()
                                                        )
                                                        .placeholder(R.drawable.profile_icon)
                                                        .into(binding.senderPostProfile)
                                                }
                                                binding.senderPostAuthor.text =
                                                    value.getString("username").toString()
                                            }
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                binding.sendMessageDatetime.text = msg.datetime
            } else
                binding.senderTv.text = msg.message

        }

    }
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding:ReceiverSampleMessageBinding
        init {
            binding = ReceiverSampleMessageBinding.bind(itemView)
        }
        fun setData(msg: ChatModel,context: Context){
            if (msg.message == "false"){
                binding.receiverTv.visibility = View.GONE
                binding.receiverPostCard.visibility = View.VISIBLE
                FirebaseDatabase.getInstance().reference
                    .child("social_media")
                    .child("posts")
                    .child(msg.post)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                var postImg = snapshot.child("post_url").getValue().toString()
                                binding.receiverPostIv.setImageBitmap(Constants().decodeImage(postImg))
                                FirebaseFirestore.getInstance().collection(Constants().KEY_COLLECTION_USERS)
                                    .document(snapshot.child("post_author").getValue().toString())
                                    .addSnapshotListener { value, error ->
                                        if(error != null)
                                            return@addSnapshotListener
                                        if(value != null){
                                            if (value.exists()){
                                                if (context != null){
                                                    Glide.with(context)
                                                        .load(value.getString("profile_pic").toString())
                                                        .placeholder(R.drawable.profile_icon)
                                                        .into(binding.receiverPostProfile)
                                                }
                                                binding.receiverPostAuthor.text = value.getString("username").toString()
                                            }
                                        }
                                    }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                binding.receiveMessageDatetime.text = msg.datetime
            }else
                binding.receiverTv.text = msg.message
        FirebaseFirestore.getInstance().collection(Constants().KEY_COLLECTION_USERS)
                .document(msg.senderid)
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        Glide.with(binding.receiverMsgProfile.context)
                            .load(it["profile_pic"].toString())
                            .placeholder(R.drawable.profile_icon)
                            .into(binding.receiverMsgProfile)
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_SENT){
            return SentViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.sender_sample_message,parent,false))
        }
        else{
            return ReceiveViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.receiver_sample_message,parent,false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT)
            SentViewHolder(holder.itemView).setData(msglist[position],context)
        else
            ReceiveViewHolder(holder.itemView).setData(msglist[position],context)
    }

    override fun getItemCount(): Int {
        return msglist.size
    }
}
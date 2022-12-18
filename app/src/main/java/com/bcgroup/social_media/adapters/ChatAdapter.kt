package com.bcgroup.social_media.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.ReceiverSampleMessageBinding
import com.bcgroup.databinding.SenderSampleMessageBinding
import com.bcgroup.social_media.activities.SocialMediaActivity
import com.bcgroup.social_media.models.ChatModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

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
                binding.senderPostCard.setOnClickListener {
                    var i = Intent(context,SocialMediaActivity::class.java)
                    i.putExtra("location","post")
                    i.putExtra("post_id",msg.post)
                    context.startActivity(i)
                }
                FirebaseDatabase.getInstance().reference
                    .child("social_media")
                    .child("posts")
                    .child(msg.post)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var postImg = snapshot.child("post_url").getValue().toString()
                                if (postImg.contains("https://")) {
                                    binding.senderPostIv.visibility = View.GONE
                                    binding.senderReelIv.visibility = View.VISIBLE
                                    val requestOptions = RequestOptions()
                                    requestOptions.isMemoryCacheable
                                    Glide.with(context!!).setDefaultRequestOptions(requestOptions)
                                        .load(postImg)
                                        .placeholder(R.drawable.video_file_icon)
                                        .into(binding.senderReelIv)

                                } else {
                                    binding.senderReelIv.visibility = View.GONE
                                    binding.senderPostIv.setImageBitmap(
                                        Constants().decodeImage(
                                            postImg
                                        )
                                    )
                                }
                                FirebaseFirestore.getInstance()
                                    .collection(Constants().KEY_COLLECTION_USERS)
                                    .document(
                                        snapshot.child("post_author").getValue().toString()
                                    )
                                    .get()
                                    .addOnSuccessListener {
                                        if (it.exists()) {
                                            if (context != null) {
                                                Glide.with(context.applicationContext)
                                                    .load(
                                                        it.getString("profile_pic")
                                                            .toString()
                                                    )
                                                    .placeholder(R.drawable.profile_icon)
                                                    .into(binding.senderPostProfile)
                                            }
                                            binding.senderPostAuthor.text =
                                                it.getString("username").toString()
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            } else
                binding.senderTv.text = msg.message
            binding.sendMessageDatetime.text = msg.datetime
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
                binding.receiverPostCard.setOnClickListener {
                    var i = Intent(context,SocialMediaActivity::class.java)
                    i.putExtra("location","post")
                    i.putExtra("post_id",msg.post)
                    context.startActivity(i)
                }
                FirebaseDatabase.getInstance().reference
                    .child("social_media")
                    .child("posts")
                    .child(msg.post)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var postImg = snapshot.child("post_url").getValue().toString()
                                if (postImg.contains("https://")) {
                                    binding.receiverPostIv.visibility = View.GONE
                                    binding.receiverReelIv.visibility = View.VISIBLE
                                    val requestOptions = RequestOptions()
                                    requestOptions.isMemoryCacheable
                                    Glide.with(context!!).setDefaultRequestOptions(requestOptions)
                                        .load(postImg)
                                        .placeholder(R.drawable.video_file_icon)
                                        .into(binding.receiverReelIv)

                                } else {
                                    binding.receiverReelIv.visibility = View.GONE
                                    binding.receiverPostIv.visibility = View.VISIBLE
                                    binding.receiverPostIv.setImageBitmap(
                                        Constants().decodeImage(
                                            postImg
                                        )
                                    )
                                }
                                FirebaseFirestore.getInstance()
                                    .collection(Constants().KEY_COLLECTION_USERS)
                                    .document(
                                        snapshot.child("post_author").getValue().toString()
                                    )
                                    .get()
                                    .addOnSuccessListener {
                                        if (it.exists()) {
                                            if (context != null) {
                                                Glide.with(context)
                                                    .load(
                                                        it.getString("profile_pic").toString()
                                                    )
                                                    .placeholder(R.drawable.profile_icon)
                                                    .into(binding.receiverPostProfile)
                                            }
                                            binding.receiverPostAuthor.text =
                                                it.getString("username").toString()
                                        }
                                    }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }else
                binding.receiverTv.text = msg.message
            binding.receiveMessageDatetime.text = msg.datetime
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
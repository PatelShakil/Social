package com.bcgroup.social_media.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.bcgroup.R
import com.bcgroup.account.Constants
import com.bcgroup.databinding.ReceiverSampleMessageBinding
import com.bcgroup.databinding.SenderSampleMessageBinding
import com.bcgroup.social_media.models.ChatModel

class ChatAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>{
    var msglist:List<ChatModel>
    var senderid:String

    constructor(msglist: List<ChatModel>, senderid: String) {
        this.msglist = msglist
        this.senderid = senderid
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
        fun setData(msg:ChatModel){
            binding.senderTv.text = msg.message
            binding.sendMessageDatetime.text = msg.datetime
        }

    }
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding:ReceiverSampleMessageBinding
        init {
            binding = ReceiverSampleMessageBinding.bind(itemView)
        }
        fun setData(msg: ChatModel){
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
            SentViewHolder(holder.itemView).setData(msglist[position])
        else
            ReceiveViewHolder(holder.itemView).setData(msglist[position])
    }

    override fun getItemCount(): Int {
        return msglist.size
    }
}
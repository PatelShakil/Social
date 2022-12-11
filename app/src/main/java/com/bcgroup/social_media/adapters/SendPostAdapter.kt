package com.bcgroup.social_media.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.SampleSendLayoutBinding
import com.bcgroup.social_media.models.UserModel
import com.bumptech.glide.Glide
import com.google.firebase.abt.FirebaseABTesting
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SendPostAdapter : RecyclerView.Adapter<SendPostAdapter.SendPostViewHolder> {
    var usersList : ArrayList<UserModel>
    var context : Context
    var postId : String

    constructor(usersList: ArrayList<UserModel>, context: Context,postId : String) : super() {
        this.usersList = usersList
        this.context = context
        this.postId = postId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SendPostViewHolder {
        return SendPostViewHolder(LayoutInflater.from(context).inflate(R.layout.sample_send_layout,null,true))
    }

    override fun onBindViewHolder(holder: SendPostViewHolder, position: Int) {
        holder.setData(usersList[position],postId)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }
    class SendPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding : SampleSendLayoutBinding
        init {
            binding = SampleSendLayoutBinding.bind(itemView)
        }
        lateinit var model : UserModel
        lateinit var postId: String
        var check = false
        fun setData(model: UserModel,postId: String){
            this.model = model
            this.postId = postId
            if (binding.socialUser.context != null){
                Glide.with(binding.socialUser.context)
                    .load(model.profile_pic)
                    .placeholder(R.drawable.profile_icon)
                    .into(binding.profile)
            }
            binding.username.text = model.username
            binding.followBtnSocialUser.setOnClickListener {
                if (!check)
                    sendPost()
                else
                    return@setOnClickListener
            }
        }
        fun sendPost(){
            var map :HashMap<String,Any> = HashMap()
            map.put(Constants().KEY_SENDER_ID,FirebaseAuth.getInstance().uid.toString())
            map.put(Constants().KEY_RECEIVER_ID,model.uid)
            map.put(Constants().KEY_MESSAGE,"false")
            map.put("post",postId)
            map.put(Constants().KEY_TIMESTAMP, Date())
            FirebaseFirestore.getInstance().collection(Constants().KEY_COLLECTION_CHAT).add(map)
            this.check = true
            binding.followBtnSocialUser.visibility = View.GONE
        }
    }
}
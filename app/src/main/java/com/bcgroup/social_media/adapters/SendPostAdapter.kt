package com.bcgroup.social_media.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.SampleSendLayoutBinding
import com.bcgroup.notification.ApiUtils
import com.bcgroup.notification.NotificationData
import com.bcgroup.notification.PushNotification
import com.bcgroup.social_media.models.UserModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Response
import java.util.*

class SendPostAdapter : RecyclerView.Adapter<SendPostAdapter.SendPostViewHolder> {
    var usersList : ArrayList<UserModel>
    var context : Context
    var postId : String
    var check : String

    constructor(usersList: ArrayList<UserModel>, context: Context,postId : String,check : String) : super() {
        this.usersList = usersList
        this.context = context
        this.postId = postId
        this.check = check
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SendPostViewHolder {
        return SendPostViewHolder(LayoutInflater.from(context).inflate(R.layout.sample_send_layout,null,true))
    }

    override fun onBindViewHolder(holder: SendPostViewHolder, position: Int) {
        holder.setData(usersList[position],postId,check)
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
        lateinit var senderName :String
        lateinit var c:String
        var check = false
        fun setData(model: UserModel,postId: String,c: String){
            this.model = model
            this.postId = postId
            this.c = c
            if (binding.socialUser.context != null){
                Glide.with(binding.socialUser.context)
                    .load(model.profile_pic)
                    .placeholder(R.drawable.profile_icon)
                    .into(binding.profile)
            }
            FirebaseFirestore.getInstance().collection(Constants().KEY_COLLECTION_USERS)
                .document(FirebaseAuth.getInstance().uid.toString())
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        senderName = it.getString("username").toString()
                    }
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
            var msg = ""
            if(c == "link") {
                map.put(Constants().KEY_MESSAGE, postId)
                msg = "Shared link"

            }
            else {
                map.put(Constants().KEY_MESSAGE,"false")
                map.put("post", postId)
                msg = "Shared post"
            }
            map.put(Constants().KEY_TIMESTAMP, Date())

            FirebaseFirestore.getInstance().collection(Constants().KEY_COLLECTION_CHAT).add(map)
            this.check = true
            binding.followBtnSocialUser.visibility = View.GONE
            sendNotification(PushNotification(NotificationData(senderName,msg,FirebaseAuth.getInstance().uid.toString()),model.token))
        }
        private fun sendNotification(notification: PushNotification) {
            ApiUtils.client.sendNotification(notification)
                ?.enqueue(object : retrofit2.Callback<PushNotification?> {
                    override fun onResponse(
                        call: Call<PushNotification?>,
                        response: Response<PushNotification?>
                    ) {
                    }

                    override fun onFailure(call: Call<PushNotification?>, t: Throwable) {
                        Toast.makeText(binding.profile.context, t.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        }
    }
}
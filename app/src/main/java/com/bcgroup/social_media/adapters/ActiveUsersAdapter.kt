package com.bcgroup.social_media.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.bcgroup.R
import com.bcgroup.account.Constants
import com.bcgroup.account.UserModel
import com.bcgroup.databinding.SampleUserActiveBinding
import com.bcgroup.social_media.ChatActivity

class ActiveUsersAdapter:RecyclerView.Adapter<ActiveUsersAdapter.ActiveUsersViewHolder> {
    var context: Context
    var active_users:ArrayList<String>

    constructor(context: Context, active_users: ArrayList<String>) : super() {
        this.context = context
        this.active_users = active_users
    }

    override fun onBindViewHolder(holder: ActiveUsersViewHolder, position: Int) {
        var user = active_users[position]
        FirebaseFirestore.getInstance().collection(Constants().KEY_COLLECTION_USERS)
            .document(user)
            .addSnapshotListener { value, error ->
                var user:UserModel = value?.toObject(UserModel::class.java)!!
                Glide.with(context.applicationContext!!).load(user.profile_pic).placeholder(R.drawable.profile_icon).into(holder.binding.activeProfile)
                holder.binding.activeUserName.text = user.name
                holder.binding.activeProfile.setOnClickListener{
                    var intent = Intent(context,ChatActivity::class.java)
                    intent.putExtra("uid",user.uid)
                    context.startActivity(intent)
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveUsersViewHolder {
        return ActiveUsersViewHolder(LayoutInflater.from(context).inflate(R.layout.sample_user_active,null,false))
    }

    override fun getItemCount(): Int {
         return active_users.size
    }
    class ActiveUsersViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
         var binding:SampleUserActiveBinding
         init {
             binding = SampleUserActiveBinding.bind(itemView)
         }
    }
}
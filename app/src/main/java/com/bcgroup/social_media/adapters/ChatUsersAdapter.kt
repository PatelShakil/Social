package com.bcgroup.social_media.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.SampleSocialUserBinding
import com.bcgroup.social_media.activities.ChatActivity
import com.bcgroup.social_media.models.UserModel
import java.util.*
import kotlin.collections.ArrayList

class ChatUsersAdapter: RecyclerView.Adapter<ChatUsersAdapter.ChatUsersViewHolder>{
    var users_list:ArrayList<UserModel>

    constructor( users_list: ArrayList<UserModel>) : super() {
        this.users_list = users_list
    }
    var db = FirebaseFirestore.getInstance()
    var database = FirebaseDatabase.getInstance()
    var auth = FirebaseAuth.getInstance()

    class ChatUsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding:SampleSocialUserBinding
        init {
            binding = SampleSocialUserBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUsersViewHolder {
        return ChatUsersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.sample_social_user,null,false))
    }

    override fun onBindViewHolder(holder: ChatUsersViewHolder, position: Int) {
        var user = users_list[position]
        holder.binding.username.text = user.name
        Glide.with(holder.binding.profile.context).load(user.profile_pic).placeholder(R.drawable.profile_icon).into(holder.binding.profile)
        holder.binding.socialUser.setOnClickListener {
            var intent = Intent(holder.binding.socialUser.context, ChatActivity::class.java)
            var bundle= Bundle()
            bundle.putString("uid",user.uid)
            bundle.putString("profile_pic",user.profile_pic)
            intent.putExtras(bundle)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            holder.binding.socialUser.context.startActivity(intent)
        }
        database.reference.child("users")
            .child(auth.uid.toString())
            .child("following")
            .child(user.uid)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        holder.binding.followBtnSocialUser.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        holder.binding.followBtnSocialUser.setOnClickListener {
            database.reference.child("users")
                .child(auth.uid.toString())
                .child("following")
                .child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            database.reference.child("users")
                                .child(auth.uid.toString())
                                .child("following")
                                .child(user.uid)
                                .setValue(null)
                        } else {
                            database.reference.child("users")
                                .child(auth.uid.toString())
                                .child("following")
                                .child(user.uid)
                                .setValue(Date().time)
                            holder.binding.followBtnSocialUser.visibility = View.GONE
                            Toast.makeText(holder.binding.followBtnSocialUser.context,"You just started following "+user.name,Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            database.reference.child("users")
                .child(user.uid)
                .child(Constants().KEY_FOLLOWERS)
                .child(auth.uid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            database.reference.child("users")
                                .child(user.uid.toString())
                                .child(Constants().KEY_FOLLOWERS)
                                .child(auth.uid.toString())
                                .setValue(null)
                        } else {
                            database.reference.child("users")
                                .child(user.uid)
                                .child(Constants().KEY_FOLLOWERS)
                                .child(auth.uid.toString())
                                .setValue(Date().time)
                            holder.binding.followBtnSocialUser.setBackgroundResource(R.drawable.following_btn_background)
                            holder.binding.followBtnSocialUser.text = "Following"
                        }
                    }
                })
        }
    }

    override fun getItemCount(): Int {
        return users_list.size
    }
}
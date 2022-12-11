package com.bcgroup.social_media.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.bcgroup.R
import com.bcgroup.databinding.FragmentViewUserProfileBinding
import com.bcgroup.classes.Constants
import com.bcgroup.social_media.activities.ChatActivity
import com.bcgroup.social_media.adapters.ProfilePostAdapter
import com.bcgroup.social_media.models.PostModel
import java.util.*
import kotlin.collections.ArrayList

class ViewUserProfileFragment : Fragment() {
    lateinit var binding: FragmentViewUserProfileBinding
    var db = FirebaseFirestore.getInstance()
    var database = FirebaseDatabase.getInstance()
    var auth = FirebaseAuth.getInstance()
    lateinit var post_adapter:ProfilePostAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewUserProfileBinding.inflate(layoutInflater)
        var user_view : String = arguments?.getString("uid").toString()
        binding.viewMessageBtn.setOnClickListener{
            var intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("uid",user_view)
            startActivity(intent)
        }
        if(user_view == auth.uid.toString()){
            binding.viewFollowBtn.visibility = View.GONE
            binding.viewMessageBtn.visibility = View.GONE
        }
        db.collection(com.bcgroup.classes.Constants().KEY_COLLECTION_USERS)
            .document(user_view)
            .addSnapshotListener { value, error ->
                if (error != null)
                    return@addSnapshotListener
                if (value != null){
                    binding.userMyProfileUserName.text = value["name"].toString()
                    binding.userMyProfileBio.text = value["bio"].toString()
                    try{
                        Glide.with(context?.applicationContext!!)
                            .load(value["profile_pic"].toString())
                            .placeholder(R.drawable.profile_icon)
                            .into(binding.viewUserProfilePic)
                    }catch(e : Exception){
                        Log.d("$context",e.message.toString())
                    }
                }
            }
        database.reference.child("users")
            .child(user_view)
            .child(Constants().KEY_FOLLOWERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var list = ArrayList<String>()
                    if (snapshot.exists()){
                        for (i in snapshot.children){
                            list.add(i.key.toString())
                        }
                        binding.followerCount.text = list.size.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {                }
            })
        database.reference.child("users")
            .child(user_view)
            .child(Constants().KEY_FOLLOWING)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var list = ArrayList<String>()
                    if (snapshot.exists()){
                        for (i in snapshot.children){
                            list.add(i.key.toString())
                        }
                        binding.followingCount.text = list.size.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {                }
            })
        var per_post_list = ArrayList<PostModel>()
        post_adapter = ProfilePostAdapter(context?.applicationContext!!,per_post_list,parentFragmentManager)
        database.reference.child("social_media")
            .child("posts")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    per_post_list.clear()
                    if (snapshot.exists()){
                        for(snapshot1 in snapshot.children){
                            if (snapshot1.child("post_author").value.toString() == user_view){
                                per_post_list.add(snapshot1.getValue(PostModel::class.java)!!)
                            }
                        }
                        post_adapter.notifyDataSetChanged()
                        binding.postCount.text = per_post_list.size.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        binding.uploadedPostRv.adapter = post_adapter
        database.reference.child("users")
            .child(auth.uid.toString())
            .child(Constants().KEY_FOLLOWING)
            .child(user_view)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        binding.viewFollowBtn.text = "Following"
                        binding.viewFollowBtn.setBackgroundResource((R.drawable.following_btn_background))
                        binding.viewMessageBtn.setBackgroundResource((R.drawable.following_btn_background))
                    }
                    else{
                        binding.viewFollowBtn.text = "Follow"
                        binding.viewFollowBtn.setBackgroundResource(R.drawable.follow_btn_background)
                        binding.viewMessageBtn.setBackgroundResource(R.drawable.follow_btn_background)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        binding.viewFollowBtn.setOnClickListener{
            database.reference.child("users")
                .child(auth.uid.toString())
                .child(Constants().KEY_FOLLOWING)
                .child(user_view)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            database.reference.child("users")
                                .child(auth.uid.toString())
                                .child(Constants().KEY_FOLLOWING)
                                .child(user_view)
                                .setValue(null)
                            database.reference.child("users")
                                .child(user_view)
                                .child(Constants().KEY_FOLLOWERS)
                                .child(auth.uid.toString())
                                .setValue(null)
                            binding.viewFollowBtn.text = "Follow"
                            binding.viewFollowBtn.setBackgroundResource(R.drawable.follow_btn_background)
                            binding.viewMessageBtn.setBackgroundResource(R.drawable.follow_btn_background)
                        }
                        else{
                            database.reference.child("users")
                                .child(auth.uid.toString())
                                .child(Constants().KEY_FOLLOWING)
                                .child(user_view)
                                .setValue(Date().time)
                            database.reference.child("users")
                                .child(user_view)
                                .child(Constants().KEY_FOLLOWERS)
                                .child(auth.uid.toString())
                                .setValue(Date().time)
                            binding.viewFollowBtn.text = "Following"
                            binding.viewFollowBtn.setBackgroundResource((R.drawable.following_btn_background))
                            binding.viewMessageBtn.setBackgroundResource((R.drawable.following_btn_background))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {                    }
                })
        }
        return binding.root
    }
}
package com.bcgroup.social_media.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bcgroup.R
import com.bcgroup.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.bcgroup.account.Constants
import com.bcgroup.databinding.SampleSocialPostProfileBinding
import com.bcgroup.social_media.adapters.ProfileTabAdapter

class ProfileFragment : Fragment() {
    lateinit var binding:FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var db :FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()
        db.collection(Constants().KEY_COLLECTION_USERS)
                    .document(auth.uid.toString())
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()){
                            binding.userMyProfileUserName.text = it["name"].toString()
                            Glide.with(binding.userMyProfilePic.context)
                                .load(it["profile_pic"].toString())
                                .placeholder(R.drawable.profile_icon)
                                .into(binding.userMyProfilePic)
                            binding.userMyProfileBio.text = it["bio"].toString()
                        }
                    }
        binding.myProfileEditProfileBtn.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.main_container,EditProfileFragment(),"edit_profile").addToBackStack("edit profile").commit()
        }
        binding.addPost.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.main_container,AddPostFragment(),"add_post").addToBackStack("add_post").commit()
        }
        database.reference.child("users")
            .child(auth.uid.toString())
            .child(Constants().KEY_FOLLOWERS)
            .addListenerForSingleValueEvent(object :ValueEventListener{
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
            .child(auth.uid.toString())
            .child(Constants().KEY_FOLLOWING)
            .addListenerForSingleValueEvent(object :ValueEventListener{
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
        var per_post_list = ArrayList<String>()
        database.reference.child("social_media")
            .child("posts")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for(snapshot1 in snapshot.children){
                            if (snapshot1.child("post_author").value.toString() == auth.uid.toString()){
                                per_post_list.add(snapshot1.child("post_url").value.toString())
                            }
                        }
                        binding.postCount.text = per_post_list.size.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        binding.profileTab.setupWithViewPager(binding.profileViewpager)
        var pager_adapter = ProfileTabAdapter(childFragmentManager)
        pager_adapter.addFragment(PostFragment(),"post")
        pager_adapter.addFragment(SavedFragment(),"saved")
        binding.profileViewpager.adapter = pager_adapter
        return binding.root
    }
    class ProfilePostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var binding: SampleSocialPostProfileBinding
        init {
            binding = SampleSocialPostProfileBinding.bind(itemView)
        }
    }
}
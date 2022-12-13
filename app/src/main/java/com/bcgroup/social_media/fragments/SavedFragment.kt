package com.bcgroup.social_media.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bcgroup.databinding.FragmentSavedBinding
import com.bcgroup.social_media.adapters.ProfilePostAdapter
import com.bcgroup.social_media.models.PostModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SavedFragment : Fragment() {
    lateinit var binding : FragmentSavedBinding
    var database = FirebaseDatabase.getInstance()
    var auth = FirebaseAuth.getInstance()
    lateinit var post_adapter :ProfilePostAdapter
    var post_list = ArrayList<PostModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedBinding.inflate(layoutInflater)
        post_list = ArrayList<PostModel>()
        database.reference.child("users")
            .child(auth.uid.toString())
            .child("saved")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        var p = ArrayList<PostModel>()
                        for (i in snapshot.children) {
                            database.reference.child("social_media")
                                .child("posts")
                                .child(i.key.toString())
                                .addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if(snapshot.exists()) {
                                            var post = PostModel()
                                            post.post_id = snapshot.child("post_id").getValue().toString()
                                            post.post_caption = snapshot.child("post_caption").getValue().toString()
                                            post.post_author = snapshot.child("post_author").getValue().toString()
                                            post.post_url = snapshot.child("post_url").getValue().toString()
                                            post.post_time = snapshot.child("post_time").getValue().toString().toLong()
                                            post.post_like = snapshot.child("post_like").getValue().toString().toLong()
                                            post_list.add(post)
                                        }
                                        post_list.reverse()
                                        post_adapter = ProfilePostAdapter(binding.savedPostRv.context,post_list,requireParentFragment().parentFragmentManager)
                                        post_adapter.notifyDataSetChanged()
                                        binding.savedPostRv.adapter = post_adapter
                                    }
                                    override fun onCancelled(error: DatabaseError) {                                    }
                                })
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {                }
            })
        return binding.root
    }
}
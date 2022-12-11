package com.bcgroup.social_media.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.bcgroup.databinding.FragmentSavedBinding
import com.bcgroup.social_media.adapters.ProfilePostAdapter
import com.bcgroup.social_media.models.PostModel

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
        database.reference.keepSynced(true)
        post_list = ArrayList<PostModel>()
        post_adapter = ProfilePostAdapter(binding.savedPostRv.context,post_list,requireParentFragment().parentFragmentManager)
        loadSavedPost()
        post_adapter.notifyDataSetChanged()
        return binding.root
    }

    private fun loadSavedPost() {
        database.reference.child("users")
            .child(auth.uid.toString())
            .child("saved")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    post_list.clear()
                    if(snapshot.exists()){
                        for (i in snapshot.children) {
                            database.reference.child("social_media")
                                .child("posts")
                                .child(i.key.toString())
                                .addValueEventListener(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if(snapshot.exists()){
                                            post_list.add(snapshot.getValue(PostModel::class.java)!!)
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {                                    }
                                })
                        }
                        post_list.reverse()
                        post_adapter.notifyDataSetChanged()
                        binding.savedPostRv.adapter = post_adapter
                    }
                    else{
                    }
                }

                override fun onCancelled(error: DatabaseError) {                }
            })
    }
}
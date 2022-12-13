package com.bcgroup.social_media.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bcgroup.databinding.FragmentPostViewBinding
import com.bcgroup.social_media.adapters.PostAdapter
import com.bcgroup.social_media.models.PostModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostViewFragment : Fragment() {
    lateinit var binding:FragmentPostViewBinding
    lateinit var post_adapter :PostAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentPostViewBinding.inflate(layoutInflater)
        var bundle = this.arguments
        if (bundle!=null){
            var post_list = ArrayList<PostModel>()
            post_adapter = PostAdapter(requireContext(),post_list,parentFragmentManager)
            binding.postRv.adapter = post_adapter
            FirebaseDatabase.getInstance().reference
                .child("social_media")
                .child("posts")
                .child(bundle.getString("post_id").toString())
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        post_list.clear()
                        if (snapshot.exists()){
                            post_list.add(snapshot.getValue(PostModel::class.java)!!)
                            post_adapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        if (post_adapter.play)
            post_adapter.releasePlayer()
    }
}
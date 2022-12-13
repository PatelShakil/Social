package com.bcgroup.social_media.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.FragmentPostBinding
import com.bcgroup.social_media.models.PostModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

class PostFragment : Fragment() {
    lateinit var binding : FragmentPostBinding
    var database = FirebaseDatabase.getInstance()
    var auth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(layoutInflater)
        var post_list = ArrayList<String>()
        var query : Query = database.reference.child("social_media").child("posts")
            .orderByChild("post_author").equalTo(auth.uid)
        val options: FirebaseRecyclerOptions<PostModel> = FirebaseRecyclerOptions.Builder<PostModel>()
            .setQuery(query, PostModel::class.java)
            .build()
        var adapter = object : FirebaseRecyclerAdapter<PostModel, ProfileFragment.ProfilePostViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileFragment.ProfilePostViewHolder {
                return ProfileFragment.ProfilePostViewHolder(
                    LayoutInflater.from(context?.applicationContext)
                        .inflate(R.layout.sample_social_post_profile, parent, false)
                )
            }
            override fun onBindViewHolder(
                holder: ProfileFragment.ProfilePostViewHolder,
                position: Int,
                post: PostModel
            ) {
                if(post.post_url.contains("https://")){
                    val requestOptions = RequestOptions()
                    requestOptions.isMemoryCacheable
                    Glide.with(context?.applicationContext!!).setDefaultRequestOptions(requestOptions).load(post.post_url).into(holder.binding.postUploaded)
                }else {
                    holder.binding.postUploaded.visibility = View.VISIBLE
                    holder.binding.postUploaded.setImageBitmap(Constants().decodeImage(post.post_url))
                }
//                    holder.binding.postUploaded.visibility = View.VISIBLE
//                   holder.binding.postUploaded.setImageBitmap(Constants().decodeImage(model.post_url))
                holder.binding.postUploaded.setOnClickListener {
//                    Toast.makeText(context,"oops... This feature currently disable", Toast.LENGTH_SHORT).show()
                       var fg = PostViewFragment()
                       var bundle = Bundle()
                       bundle.putString("post_id",post.post_id)
                       fg.arguments = bundle
                    requireParentFragment().parentFragmentManager.beginTransaction().replace(R.id.main_container,fg,"post_view").addToBackStack("post_view").commit()
                }
            }
        }
        binding.uploadedPostRv.adapter = adapter
        adapter.startListening()
        return binding.root
    }
}
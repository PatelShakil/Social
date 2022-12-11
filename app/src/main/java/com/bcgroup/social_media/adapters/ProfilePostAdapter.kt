package com.bcgroup.social_media.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.SampleSocialPostProfileBinding
import com.bcgroup.social_media.fragments.PostViewFragment
import com.bcgroup.social_media.models.PostModel


class ProfilePostAdapter: RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder>{
    var context:Context
    var post_list:ArrayList<PostModel>
    var fm:FragmentManager

    constructor(context: Context, post_list: ArrayList<PostModel>, fm:FragmentManager) : super() {
        this.context = context
        this.post_list = post_list
        this.fm = fm
    }

    class ProfilePostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var binding:SampleSocialPostProfileBinding
        init {
            binding = SampleSocialPostProfileBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.sample_social_post_profile,parent,false)
        return ProfilePostViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostViewHolder, position: Int) {
        if(post_list[position].post_url.contains("https://")){
            val requestOptions = RequestOptions()
            requestOptions.isMemoryCacheable
            Glide.with(context).setDefaultRequestOptions(requestOptions).load(post_list[position].post_url).placeholder(R.drawable.video_file_icon).into(holder.binding.postUploaded)
        }else {
            holder.binding.postUploaded.setImageBitmap(Constants().decodeImage(post_list[position].post_url))
        }
        holder.binding.postUploaded.setOnClickListener {
            var fragment = PostViewFragment()
            var bundle = Bundle()
            bundle.putString("post_id",post_list[position].post_id)
            fragment.arguments = bundle
            fm.beginTransaction().replace(R.id.main_container,fragment,"post_view").addToBackStack("post_view").commit()
        }
    }

    override fun getItemCount(): Int {
        return post_list.size
    }
}
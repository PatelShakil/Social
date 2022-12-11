package com.bcgroup.social_media.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.FragmentHomeBinding
import com.bcgroup.social_media.adapters.PostAdapter
import com.bcgroup.social_media.models.PostModel
import java.util.*


class HomeFragment : Fragment() {
    lateinit var binding :FragmentHomeBinding
    lateinit var database:FirebaseDatabase
    lateinit var post_list:ArrayList<PostModel>
    lateinit var adapter :PostAdapter
    lateinit var adapter_feed:FirebaseRecyclerAdapter<PostModel,PostAdapter.PostViewHolder>
    var player:ExoPlayer? = null
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    lateinit var hol :View
    lateinit var u:String
    var play = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        database = FirebaseDatabase.getInstance()
        post_list = ArrayList()
        post_list.clear()
        adapter = PostAdapter(context?.applicationContext!!,post_list,parentFragmentManager)
        var anim = AnimationUtils.loadAnimation(binding.homeFragment.context,R.anim.slide_down_anim)
        binding.homeFragment.animation = anim
        binding.swipe.setOnRefreshListener {
            adapter.notifyDataSetChanged()
            binding.swipe.isRefreshing = false
        }
        database.reference.child("social_media")
            .child("posts")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    post_list.clear()
                    if (snapshot.exists()){
                        for (i in snapshot.children){
                            var post: PostModel? =i.getValue(PostModel::class.java)
                            post?.post_id = i.key.toString()
                            if (post != null) {
                                post_list.add(0,post)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        val options: FirebaseRecyclerOptions<PostModel> = FirebaseRecyclerOptions.Builder<PostModel>()
            .setQuery(database.reference.child("social_media")
                .child("posts"),PostModel::class.java)
            .build()
        adapter_feed = object : FirebaseRecyclerAdapter<PostModel, PostAdapter.PostViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.PostViewHolder {
                return PostAdapter.PostViewHolder(
                    LayoutInflater.from(context?.applicationContext)
                        .inflate(R.layout.sample_social_post, parent, false)
                )
            }

            override fun onBindViewHolder(
                holder: PostAdapter.PostViewHolder,
                position: Int,
                post: PostModel
            ) {
                var db = FirebaseFirestore.getInstance()
                var auth = FirebaseAuth.getInstance()
                var post_author_name = ""
                var curren_user_name = ""
                holder.binding.userPostCaption.text = post.post_caption
                if (post.post_url.contains("https://")) {
                    try {
                        holder.binding.userPostVid.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                        holder.binding.userPost.visibility = View.GONE
                        holder.binding.userPostVid.visibility = View.GONE
                        holder.binding.userPostVidThumb.visibility = View.VISIBLE
                        holder.binding.userPostVidThumb.setOnClickListener {
                            holder.binding.userPostVid.visibility = View.VISIBLE
                            holder.binding.userPostVid.controllerShowTimeoutMs = 10
                            holder.binding.userPostVidThumb.visibility = View.GONE
                            if (Util.SDK_INT > 23) {
                                releasePlayer()
                                initializePlayer(holder.itemView,post.post_url)
                            }
                        }
                        holder.binding.userPostVid.setOnTouchListener { p0, p1 ->
                            releasePlayer()
                            holder.binding.userPost.visibility = View.GONE
                            holder.binding.userPostVid.visibility = View.GONE
                            holder.binding.userPostVidThumb.visibility = View.VISIBLE
                            true
                        }
                        val requestOptions = RequestOptions()
                        requestOptions.isMemoryCacheable
                        Glide.with(context!!).setDefaultRequestOptions(requestOptions).load(post.post_url)
                            .placeholder(R.drawable.video_file_icon)
                            .into(holder.binding.userPostVidThumb)
                        holder.binding.userPostVid.dispatchWindowFocusChanged(true)
                        u = post.post_url
                        hol = holder.itemView

//                holder.binding.userPostVideo.setVideoURI(Uri.parse(post.post_url))
//                        holder.binding.userPostVideo.visibility = View.VISIBLE
//                        holder.binding.userPost.visibility = View.GONE
////                holder.binding.userPostVideo.setMediaController(MediaController(context))
////                holder.binding.userPostVideo.start()
//                        prepareExoplayer(holder.itemView,post.post_url)
                    }catch (e:Exception) {
                        Toast.makeText(context,e.message, Toast.LENGTH_LONG).show()
                    }
                }else {
                    holder.binding.userPost.visibility = View.VISIBLE
                    holder.binding.userPostVid.visibility = View.GONE
                    holder.binding.userPostVidThumb.visibility = View.GONE
                    holder.binding.userPost.setImageBitmap(Constants().decodeImage(post.post_url))
                }
                db.collection(Constants().KEY_COLLECTION_USERS)
                    .document(post.post_author)
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            holder.binding.userName.text = it["name"].toString()
                            post_author_name = it["name"].toString()
                            try{
                                Glide.with(context?.applicationContext!!)
                                    .load(it["profile_pic"])
                                    .placeholder(R.drawable.profile_icon)
                                    .into(holder.binding.userProfile)
                            }catch(e :Exception){                            }

                        }
                    }
                db.collection(Constants().KEY_COLLECTION_USERS)
                    .document(FirebaseAuth.getInstance().uid.toString())
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            curren_user_name = it["name"].toString()
                            try {
                                Glide.with(context?.applicationContext!!)
                                    .load(it["profile_pic"])
                                    .placeholder(R.drawable.profile_icon)
                                    .into(holder.binding.currentUserProfile)
                            }catch (e : Exception){}
                        }
                    }
                holder.binding.postLikeCount.text = post.post_like.toString()
                database.reference.child("social_media")
                    .child("posts")
                    .child(post.post_id)
                    .child("likes")
                    .child(auth.uid.toString())
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                holder.binding.postLikeBtn.setBackgroundResource(R.drawable.ic_like_red)
                            } else
                                holder.binding.postLikeBtn.setBackgroundResource(R.drawable.ic_like)
                        }

                        override fun onCancelled(error: DatabaseError) {}

                    })
                holder.binding.postLikeBtn.setOnClickListener {
                    database.reference.child("social_media")
                        .child("posts")
                        .child(post.post_id)
                        .child("likes")
                        .child(auth.uid.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    database.reference.child("social_media")
                                        .child("posts")
                                        .child(post.post_id)
                                        .child("likes")
                                        .child(auth.uid.toString())
                                        .setValue(null)
                                        .addOnSuccessListener {
                                            database.reference.child("social_media")
                                                .child("posts")
                                                .child(post.post_id)
                                                .child("post_like")
                                                .setValue(post.post_like - 1)
                                                .addOnSuccessListener {
                                                    holder.binding.postLikeBtn.setBackgroundResource(R.drawable.ic_like)
                                                }
                                        }
                                } else {
                                    database.reference.child("social_media")
                                        .child("posts")
                                        .child(post.post_id)
                                        .child("likes")
                                        .child(auth.uid.toString())
                                        .setValue(Date().time)
                                        .addOnSuccessListener {
                                            database.reference.child("social_media")
                                                .child("posts")
                                                .child(post.post_id)
                                                .child("post_like")
                                                .setValue(post.post_like + 1)
                                                .addOnSuccessListener {
                                                    holder.binding.postLikeBtn.setBackgroundResource(R.drawable.ic_like_red)
                                                }
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}

                        })

                }
                holder.binding.addYourComment.doOnTextChanged { text, start, before, count ->
                    if (text != null && text.isNotEmpty())
                        holder.binding.sendPostComment.visibility = View.VISIBLE
                    else
                        holder.binding.sendPostComment.visibility = View.GONE
                }
                database.reference.child("users")
                    .child(auth.uid.toString())
                    .child("saved")
                    .child(post.post_id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                holder.binding.postSave.setBackgroundResource(R.drawable.saved)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                holder.binding.postSave.setOnClickListener {
                    database.reference.child("users")
                        .child(auth.uid.toString())
                        .child("saved")
                        .child(post.post_id)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    database.reference.child("users")
                                        .child(auth.uid.toString())
                                        .child("saved")
                                        .child(post.post_id)
                                        .setValue(null)
                                        .addOnSuccessListener {
                                            holder.binding.postSave.setBackgroundResource(R.drawable.save_outline)
                                        }
                                } else {
                                    database.reference.child("users")
                                        .child(auth.uid.toString())
                                        .child("saved")
                                        .child(post.post_id)
                                        .setValue(Date().time)
                                        .addOnSuccessListener {
                                            holder.binding.postSave.setBackgroundResource(R.drawable.saved)
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
                holder.binding.postUserLayout.setOnClickListener{
                    var fragment = ViewUserProfileFragment()
                    var bundle = Bundle()
                    bundle.putString("uid",post.post_author)
                    fragment.arguments = bundle
//                    (BottomNavigationView(context?.applicationContext!!)).findViewById<BottomNavigationView>(R.id.navigation).visibility = View.GONE
                    parentFragmentManager.beginTransaction().replace(R.id.main_container,fragment).addToBackStack("view_user").commit()
                }
            }
        }
        binding.feedPostRv.adapter = adapter_feed
        adapter_feed.startListening()

        return binding.root
    }
//    private fun prepareExoplayer(itemView:View, vurl:String) {
//        try {
//            Log.d("Hear","Alive")
//            val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
//            val trackSelector: TrackSelector =
//                DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))
//            var exoPlayer=
//                ExoPlayerFactory.newSimpleInstance(this.context, trackSelector) as SimpleExoPlayer
//
//            val videoURI = Uri.parse(vurl)
//
//            val dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer_video")
//            val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
//            val mediaSource: MediaSource =
//                ExtractorMediaSource(videoURI, dataSourceFactory, extractorsFactory, null, null)
//
//            itemView.findViewById<SimpleExoPlayerView>(R.id.user_post_video).player = exoPlayer
//            exoPlayer.prepare(mediaSource)
//            exoPlayer.playWhenReady = false
//
//        }catch (e:Exception){
//            Log.d("EXO",e.message.toString())
//        }
//    }

    override fun onStart() {
        super.onStart()
        adapter_feed.notifyDataSetChanged()
    }
    override fun onResume() {
        super.onResume()
        adapter_feed.notifyDataSetChanged()
    }
    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }


    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }
    private fun initializePlayer(holder:View,url:String) {
        player = ExoPlayer.Builder(context?.applicationContext!!)
            .build()
            .also { exoPlayer ->
                val mediaItem = MediaItem.fromUri(Uri.parse(url))
                exoPlayer.setMediaItem(mediaItem)
                holder.findViewById<PlayerView>(R.id.user_post_vid).player = exoPlayer
//                exoPlayer.playWhenReady = playWhenReady
//                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.prepare()
                exoPlayer.play()
                play = true
            }
    }
    private fun releasePlayer() {
        player?.let { exoPlayer ->
//            playbackPosition = exoPlayer.currentPosition
//            currentItem = exoPlayer.currentMediaItemIndex
//            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
            play = false
        }
        player = null
    }
}
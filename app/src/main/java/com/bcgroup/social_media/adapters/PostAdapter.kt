package com.bcgroup.social_media.adapters

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.SampleSocialPostBinding
import com.bcgroup.social_media.fragments.ViewUserProfileFragment
import com.bcgroup.social_media.models.PostModel
import com.bcgroup.social_media.models.UserModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class PostAdapter:RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    var context:Context
    var post_list:ArrayList<PostModel>
    var fm : FragmentManager
    fun releasePlayer() {
        player.let { exoPlayer ->
//        playbackPosition = exoPlayer.currentPosition
//        currentItem = exoPlayer.currentMediaItemIndex
//        playWhenReady = exoPlayer.playWhenReady
            exoPlayer?.release()
            play = false
        }
        player = null
    }
    constructor(context: Context, post_list: ArrayList<PostModel>,fm:FragmentManager) : super() {
        this.context = context
        this.post_list = post_list
        this.fm = fm
    }
    var database:FirebaseDatabase = FirebaseDatabase.getInstance()
    var auth = FirebaseAuth.getInstance()
    var player:ExoPlayer? = null
    var playWhenReady = true
    var currentItem = 0
    var playbackPosition = 0L
    var play = false
    lateinit var hol :View
    lateinit var u:String

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding:SampleSocialPostBinding
        init {
            binding = SampleSocialPostBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.sample_social_post,null,false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        var post = post_list[position]
        var db = FirebaseFirestore.getInstance()
        var auth = FirebaseAuth.getInstance()
        var post_author_name = ""
        var curren_user_name = ""
        holder.binding.userPostCaption.text = post.post_caption
        holder.binding.postShare.setOnClickListener {
            var sheet : BottomSheetDialog
            sheet = BottomSheetDialog(context)
            sheet.setContentView(R.layout.send_layout)
            var rv = sheet.findViewById<RecyclerView>(R.id.send_users_rv)
            sheet.findViewById<TextView>(R.id.send_post_caption)?.text = post.post_caption
            var users_list = ArrayList<UserModel>()
            db.collection(Constants().KEY_COLLECTION_USERS)
                .whereNotEqualTo("uid", auth.uid.toString())
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        for (i in it.documents) {
                            var user: UserModel = i.toObject(UserModel::class.java)!!
                            user.profile_pic = i["profile_pic"].toString()
                            user.token = i["token"].toString()
                            users_list.add(user)
                        }
                    }
                    if (users_list.size > 0) {
                        var users_adapter = SendPostAdapter(users_list,holder.binding.postShare.context.applicationContext,post.post_id,"post")
                        rv?.adapter = users_adapter
                        users_adapter.notifyDataSetChanged()
                    }
                }
            sheet.show()
        }
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
                    Glide.with(context?.applicationContext!!)
                        .load(it["profile_pic"])
                        .placeholder(R.drawable.profile_icon)
                        .into(holder.binding.userProfile)
                }
            }
        db.collection(Constants().KEY_COLLECTION_USERS)
            .document(FirebaseAuth.getInstance().uid.toString())
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    curren_user_name = it["name"].toString()
                    Glide.with(context?.applicationContext!!)
                        .load(it["profile_pic"])
                        .placeholder(R.drawable.profile_icon)
                        .into(holder.binding.currentUserProfile)
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
            releasePlayer()
            var fragment = ViewUserProfileFragment()
            var bundle = Bundle()
            bundle.putString("uid",post.post_author)
            fragment.arguments = bundle
            fm.beginTransaction().replace(R.id.main_container,fragment).addToBackStack("view_user").commit()
        }
    }
    override fun getItemCount(): Int {
        return post_list.size
    }

private fun initializePlayer(holder:View,url:String) {
    player = ExoPlayer.Builder(context.applicationContext!!)
        .build()
        .also { exoPlayer ->
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            exoPlayer.setMediaItem(mediaItem)
            holder.findViewById<PlayerView>(R.id.user_post_vid).player = exoPlayer
//            exoPlayer.playWhenReady = playWhenReady
//            exoPlayer.seekTo(currentItem, playbackPosition)
            exoPlayer.prepare()
            exoPlayer.play()
            play = true
        }
}
}
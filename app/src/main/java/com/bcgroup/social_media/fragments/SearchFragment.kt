package com.bcgroup.social_media.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.FragmentSearchBinding
import com.bcgroup.databinding.SampleSocialUserBinding
import com.bcgroup.social_media.adapters.ChatUsersAdapter
import com.bcgroup.social_media.adapters.ProfilePostAdapter
import com.bcgroup.social_media.models.PostModel
import com.bcgroup.social_media.models.UserModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SearchFragment : Fragment() {
    lateinit var binding : FragmentSearchBinding
    var database = FirebaseDatabase.getInstance()
    var db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()
    var user_list = ArrayList<UserModel>()
    lateinit var user_adapter : SearchUsersAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        var post_list = ArrayList<PostModel>()
        var post_adapter = ProfilePostAdapter(context?.applicationContext!!,post_list,parentFragmentManager)
        binding.searchPostRv.adapter = post_adapter
        database.reference.child("social_media")
            .child("posts")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (i in snapshot.children){
                            post_list.add(i.getValue(PostModel::class.java)!!)
                        }
                        post_adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {                }
            })
        db.collection(Constants().KEY_COLLECTION_USERS)
            .whereNotEqualTo(Constants().KEY_USER_ID, auth.uid.toString())
            .addSnapshotListener { value, error ->
                if (error != null)
                    return@addSnapshotListener
                if (value != null) {
                    user_list.clear()
                    for (i in value.documents) {
                        var user = i.toObject(UserModel::class.java)
                        user?.profile_pic = i["profile_pic"].toString()
                        user_list.add(user!!)
                    }
                    binding.searchBar.queryHint = "Search Among " + user_list.size + " users"
                }
            }
        binding.searchBar.setOnQueryTextFocusChangeListener { view, b ->
            binding.searchPostRv.visibility = View.GONE
            binding.searchUsersRv.visibility = View.VISIBLE
            if(binding.searchBar == null){
                db.collection(Constants().KEY_COLLECTION_USERS)
                    .whereNotEqualTo(Constants().KEY_USER_ID,auth.uid.toString())
                    .addSnapshotListener { value, error ->
                        if(error != null)
                            return@addSnapshotListener
                        if (value != null){
                            for (i in value.documents){
                                var user = i.toObject(UserModel::class.java)
                                user?.profile_pic = i["profile_pic"].toString()
                                user_list.add(user!!)
                            }
                            user_adapter.notifyDataSetChanged()
                        }
                    }
            }
            if(binding.searchBar != null){
                binding.searchBar.setOnQueryTextListener(object :OnQueryTextListener{
                    override fun onQueryTextChange(p0: String?): Boolean {
                        binding.searchPostRv.visibility = View.GONE
                        binding.searchUsersRv.visibility = View.VISIBLE
                        searchUser(p0)
                        return false
                    }

                    override fun onQueryTextSubmit(p0: String?): Boolean { return false     }
                })
            }
        }
        user_adapter = SearchUsersAdapter(user_list,parentFragmentManager)
        binding.searchUsersRv.adapter = user_adapter

        return binding.root
    }
    class SearchUsersAdapter :RecyclerView.Adapter<SearchUsersAdapter.SearchUsersViewHolder>{
        var usersList : ArrayList<UserModel>
        var fm : FragmentManager

        constructor(usersList: ArrayList<UserModel>,fm : FragmentManager) : super() {
            this.usersList = usersList
            this.fm = fm
        }


        class SearchUsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            lateinit var binding : SampleSocialUserBinding
            init {
                binding = SampleSocialUserBinding.bind(itemView)
            }
            var db = FirebaseFirestore.getInstance()
            var database = FirebaseDatabase.getInstance()
            var auth = FirebaseAuth.getInstance()
            fun setData(user: UserModel,fm : FragmentManager){
                binding.username.text = user.name
                Glide.with(binding.profile.context.applicationContext).load(user.profile_pic).placeholder(R.drawable.profile_icon).into(binding.profile)
                binding.socialUser.setOnClickListener {
                    var fragment = ViewUserProfileFragment()
                    var bundle = Bundle()
                    bundle.putString("uid",user.uid)
                    fragment.arguments = bundle
                    fm.beginTransaction().replace(R.id.main_container,fragment).addToBackStack("view_user").commit()

                }
                database.reference.child("users")
                    .child(auth.uid.toString())
                    .child("following")
                    .child(user.uid)
                    .addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                binding.followBtnSocialUser.visibility = View.GONE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                binding.followBtnSocialUser.setOnClickListener {
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
                                    binding.followBtnSocialUser.visibility = View.GONE
                                    Toast.makeText(binding.followBtnSocialUser.context,"You just started following "+user.name,
                                        Toast.LENGTH_SHORT).show()
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
                                    binding.followBtnSocialUser.setBackgroundResource(R.drawable.following_btn_background)
                                    binding.followBtnSocialUser.text = "Following"
                                }
                            }
                        })
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUsersViewHolder {
            return SearchUsersAdapter.SearchUsersViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.sample_social_user, null, false)
            )
        }

        override fun onBindViewHolder(holder: SearchUsersViewHolder, position: Int) {
            holder.setData(usersList[position],fm)
        }

        override fun getItemCount(): Int {
            return usersList.size
        }
    }

    private fun searchUser(p0: String?) {
        var search_users = ArrayList<UserModel>()
        for (i in user_list){
            if (i.username.lowercase().contains(p0!!)){
                search_users.add(i)
            }
        }
        var adapter = ChatUsersAdapter(search_users)
        binding.searchUsersRv.adapter = adapter
    }
}
package com.bcgroup.social_media.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView.OnQueryTextListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.FragmentSearchBinding
import com.bcgroup.social_media.adapters.ChatUsersAdapter
import com.bcgroup.social_media.adapters.ProfilePostAdapter
import com.bcgroup.social_media.models.PostModel
import com.bcgroup.social_media.models.UserModel

class SearchFragment : Fragment() {
    lateinit var binding : FragmentSearchBinding
    var database = FirebaseDatabase.getInstance()
    var db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()
    var user_list = ArrayList<UserModel>()
    var user_adapter = ChatUsersAdapter(user_list)
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
        binding.searchUsersRv.adapter = user_adapter

        return binding.root
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
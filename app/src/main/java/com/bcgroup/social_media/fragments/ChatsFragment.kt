package com.bcgroup.social_media.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.bcgroup.R
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.bcgroup.account.Constants
import com.bcgroup.account.UserModel
import com.bcgroup.databinding.FragmentChatsBinding
import com.bcgroup.social_media.adapters.ChatUsersAdapter
import kotlin.collections.ArrayList


class ChatsFragment : Fragment() {
    lateinit var binding:FragmentChatsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    lateinit var db:FirebaseFirestore
    lateinit var users_list:ArrayList<UserModel>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()
        users_list = ArrayList<UserModel>()
        var anim = AnimationUtils.loadAnimation(binding.chatsFragment.context,R.anim.slide_down_anim)
        binding.chatsFragment.animation = anim
        db.collection(Constants().KEY_COLLECTION_USERS)
            .whereNotEqualTo("uid", auth.uid.toString())
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (i in it.documents) {
                        var user: UserModel = i.toObject(UserModel::class.java)!!
                        user.profile_pic = i["profile_pic"].toString()
                        users_list.add(user)
                    }
                }
                if (users_list.size > 0) {
                    var users_adapter = ChatUsersAdapter(users_list)
                    binding.chatUsersRv.adapter = users_adapter
                    users_adapter.notifyDataSetChanged()
                }
            }
        return binding.root
    }


}
package com.bcgroup.social_media.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.FragmentUsersBinding
import com.bcgroup.social_media.adapters.ActiveUsersAdapter
import com.bcgroup.social_media.adapters.RecentConversionAdapter
import com.bcgroup.social_media.models.ChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class UsersFragment : Fragment() {
    lateinit var binding:FragmentUsersBinding
    lateinit var db:FirebaseFirestore
    lateinit var auth:FirebaseAuth
    lateinit var conversions:ArrayList<ChatModel>
    lateinit var conadapter:RecentConversionAdapter
    lateinit var active_users:ArrayList<String>
    lateinit var active_adapter:ActiveUsersAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersBinding.inflate(layoutInflater)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        conversions = ArrayList()
        active_users = ArrayList()
        conadapter = RecentConversionAdapter(conversions)
        active_adapter = ActiveUsersAdapter(context?.applicationContext!!,active_users)
        binding.conUsers.adapter = conadapter
        var anim = AnimationUtils.loadAnimation(binding.usersFragment.context,R.anim.slide_down_anim)
        binding.usersFragment.animation = anim
        binding.addNewChatUsers.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.main_container,ChatsFragment(),"new_users").addToBackStack("new_users").commit()
        }
        conUserLoader()

        return binding.root
    }
    fun conUserLoader(){
        db.collection(Constants().KEY_COLLECTION_CONVERSIONS)
            .whereEqualTo(Constants().KEY_SENDER_ID,auth.uid.toString())
            .addSnapshotListener(eventListener)
        db.collection(Constants().KEY_COLLECTION_CONVERSIONS)
            .whereEqualTo(Constants().KEY_RECEIVER_ID,auth.uid.toString())
            .addSnapshotListener(eventListener)
    }
    private var eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null)
            return@EventListener
        if (value != null) {
            for(i in value.documentChanges){
                if(i.type == DocumentChange.Type.ADDED){
                    var senderid = i.document.getString(Constants().KEY_SENDER_ID)
                    var receiverid = i.document.getString(Constants().KEY_RECEIVER_ID)
                    var msg = ChatModel()
                    msg.senderid = senderid!!
                    msg.receiverid = receiverid!!
                    if(auth.uid.toString() == senderid){
                        msg.conversionid = i.document.getString(Constants().KEY_RECEIVER_ID).toString()
                        msg.conversionname = i.document.getString(Constants().KEY_RECEIVER_NAME).toString()
                        msg.conversionimage = i.document.getString(Constants().KEY_RECEIVER_IMAGE).toString()
                    }else{
                        msg.conversionid = i.document.getString(Constants().KEY_SENDER_ID).toString()
                        msg.conversionname = i.document.getString(Constants().KEY_SENDER_NAME).toString()
                        msg.conversionimage = i.document.getString(Constants().KEY_SENDER_IMAGE).toString()
                    }
                    msg.message = i.document.getString(Constants().KEY_LASTMESSAGE).toString()
                    msg.date = i.document.getDate(Constants().KEY_TIMESTAMP)!!
                    conversions.add(msg)
                }else if (i.type == DocumentChange.Type.MODIFIED){
                    for (con in 0..conversions.size) {
                        var senderid = i.document.getString(Constants().KEY_SENDER_ID)
                        var receiverid = i.document.getString(Constants().KEY_RECEIVER_ID)
                        if(conversions.get(con).senderid == senderid && conversions[con].receiverid == receiverid){
                            conversions[con].message = i.document.getString(Constants().KEY_LASTMESSAGE).toString()
                            conversions[con].date = i.document.getDate(Constants().KEY_TIMESTAMP)!!
                            break
                        }
                    }
                }
            }
            conversions.sortWith(Comparator { obj1, obj2 -> obj2.date.compareTo(obj1.date) })
            conadapter.notifyDataSetChanged()
            binding.conUsers.adapter = conadapter
            binding.conUsers.smoothScrollToPosition(0)
        }
    }
    fun activeUsersFetcher(){
        db.collection(Constants().KEY_COLLECTION_CONVERSIONS)
            .addSnapshotListener { value, error ->
                if (error != null)
                    return@addSnapshotListener
                if (value != null) {
                    for (i in value.documentChanges) {
                        if(i.type == DocumentChange.Type.ADDED || i.type == DocumentChange.Type.MODIFIED) {
                            if (i.document.getString(Constants().KEY_SENDER_ID) == auth.uid.toString() || i.document.getString(
                                    Constants().KEY_RECEIVER_ID
                                ) == auth.uid.toString()
                            ) {
                                if (i.document.getString(Constants().KEY_SENDER_ID) == auth.uid) {
                                    db.collection(Constants().KEY_COLLECTION_USERS)
                                        .document(
                                            i.document.getString(Constants().KEY_RECEIVER_ID)
                                                .toString()
                                        )
                                        .addSnapshotListener { value, error ->
                                            if (error != null)
                                                return@addSnapshotListener
                                            if (value != null){
                                                if(value[Constants().KEY_USERSTATUS] == 1){
                                                    active_users.add(value[Constants().KEY_USER_ID].toString())
                                                }
                                            }

                                        }
                                } else {
                                    db.collection(Constants().KEY_COLLECTION_USERS)
                                        .document(
                                            i.document.getString(Constants().KEY_SENDER_ID)
                                                .toString()
                                        )
                                        .addSnapshotListener { value, error ->
                                            if (error != null)
                                                return@addSnapshotListener
                                            if (value != null){
                                                if(value.getLong(Constants().KEY_USERSTATUS)!!.equals(1)){
                                                    active_users.add(value[Constants().KEY_USER_ID].toString())
                                                }
                                            }
                                        }
                                }
                            }
                        }
                        }
                    active_adapter.notifyDataSetChanged()
                    binding.activeUsersRv.adapter = active_adapter
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        activeUsersFetcher()
    }

    override fun onPause() {
        onDestroy()
        super.onPause()
    }
}
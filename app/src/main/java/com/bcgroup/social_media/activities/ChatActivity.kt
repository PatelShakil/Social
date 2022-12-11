package com.bcgroup.social_media.activities

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.ActivityChatBinding
import com.bcgroup.social_media.adapters.ChatAdapter
import com.bcgroup.social_media.fragments.ViewUserProfileFragment
import com.bcgroup.social_media.models.ChatModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ChatActivity : BaseActivity()  {
    lateinit var binding:ActivityChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    lateinit var msglist:MutableList<ChatModel>
    lateinit var msgAdapter:ChatAdapter
    var db = FirebaseFirestore.getInstance()
    var conversionid: String? = null
    var receiverid:String = ""
    var sname = ""
    var simage = ""
    var rname = ""
    var rimage = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.chatToolbar)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        msglist = ArrayList()
        msgAdapter = ChatAdapter(msglist,auth.uid.toString())
        receiverid = intent.extras?.getString("uid").toString()
        senderInfo()
        binding.receiverProfile.setOnClickListener {
            var fragment = ViewUserProfileFragment()
            var bundle = Bundle()
            bundle.putString("uid",receiverid)
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction().replace(R.id.chat_container,fragment,"view_user").addToBackStack("view_user").commit()
        }
        binding.msgSendBtn.setOnClickListener {
            if (binding.msgTextEt.text?.trim().toString().isNotEmpty())
                sendMessage()
        }
        binding.chatBackBtn.setOnClickListener{
            onBackPressed()
        }
        db.collection(Constants().KEY_COLLECTION_USERS)
            .document(receiverid)
            .addSnapshotListener { value, error ->
                if(error != null)
                    return@addSnapshotListener
                if (value != null) {
                    if (value.exists()) {
                        binding.receiverName.text = value["name"].toString()
                        Glide.with(applicationContext)
                            .load(value["profile_pic"])
                            .placeholder(R.drawable.profile_icon)
                            .into(binding.receiverProfile)
                        rname = value["name"].toString()
                        rimage = value["profile_pic"].toString()
                        if (Integer.parseInt(value["status"].toString()) == 1) {
                            binding.userStatus.visibility = View.VISIBLE
                            var anim = AnimationUtils.loadAnimation(this, R.anim.slide_down_anim)
                            binding.userStatus.animation = anim
                        } else {
                            binding.userStatus.visibility = View.GONE
                        }
                    }
                }
            }
        listmessages()
    }
    fun senderInfo(){
        db.collection(Constants().KEY_COLLECTION_USERS)
            .document(auth.uid.toString())
            .get()
            .addOnSuccessListener {
                sname = it["name"].toString()
                simage = it["profile_pic"].toString()
            }
    }
    private fun listmessages(){
        FirebaseFirestore.getInstance().collection(Constants().KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants().KEY_SENDER_ID,auth.uid.toString())
            .whereEqualTo(Constants().KEY_RECEIVER_ID,receiverid)
            .addSnapshotListener(eventListener)
        FirebaseFirestore.getInstance().collection(Constants().KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants().KEY_SENDER_ID,receiverid)
            .whereEqualTo(Constants().KEY_RECEIVER_ID,auth.uid.toString())
            .addSnapshotListener(eventListener)
    }
    private var eventListener = EventListener<QuerySnapshot>{ value, error ->
        if (error != null)
            return@EventListener
        if (value != null){
            var count = msglist.size
            for (i in value.documentChanges) {
                if (i.type == DocumentChange.Type.ADDED) {
                    var msg = ChatModel()
                    msg.senderid = i.document.getString(Constants().KEY_SENDER_ID).toString()
                    msg.receiverid = i.document.getString(Constants().KEY_RECEIVER_ID).toString()
                    msg.message = i.document.getString(Constants().KEY_MESSAGE).toString()
                    msg.datetime =
                        i.document.getDate(Constants().KEY_TIMESTAMP)?.let { readableDate(it) }
                            .toString()
                    msg.date = i.document.getDate(Constants().KEY_TIMESTAMP)!!
                    msglist.add(msg)
                }
            }
            msglist.sortWith(Comparator { obj1, obj2 -> obj1.date.compareTo(obj2.date) })
            if (count == 0)
                msgAdapter.notifyDataSetChanged()
            else {
                msgAdapter = ChatAdapter(msglist, auth.uid.toString())
                msgAdapter.notifyItemRangeInserted(msglist.size, msglist.size)
                binding.messagesRv.smoothScrollToPosition(msglist.size - 1)
                msgAdapter.notifyDataSetChanged()
            }
        }
        binding.messagesRv.adapter = msgAdapter
        if(conversionid == null){
            checkConversion()
        }
    }
    fun addConversion(conversion:HashMap<String,Any>){
        db.collection(Constants().KEY_COLLECTION_CONVERSIONS)
            .add(conversion)
            .addOnSuccessListener {
                conversionid = it.id
            }
    }
    private fun updateConversion(message:String){
        var df = db.collection(Constants().KEY_COLLECTION_CONVERSIONS).document(conversionid!!)
                  df.update(Constants().KEY_LASTMESSAGE,message,Constants().KEY_TIMESTAMP,Date())

    }
    private fun checkConversion(){
        if (msglist.size != 0){
            checkConversionRemotely(auth.uid.toString(),receiverid)
            checkConversionRemotely(receiverid,auth.uid.toString())
        }
    }
    private fun checkConversionRemotely(sid:String,rid:String){
        db.collection(Constants().KEY_COLLECTION_CONVERSIONS)
            .whereEqualTo(Constants().KEY_SENDER_ID,sid)
            .whereEqualTo(Constants().KEY_RECEIVER_ID,rid)
            .get()
            .addOnCompleteListener(conversionlistener)
    }
    private var conversionlistener = OnCompleteListener<QuerySnapshot> {
        if(it.isSuccessful && it.result != null && it.result.documents.size > 0){
            var ds = it.result.documents[0]
            conversionid = ds.id
        }
    }
    fun sendMessage(){
        var map :HashMap<String,Any> = HashMap()
        map.put(Constants().KEY_SENDER_ID,auth.uid.toString())
        map.put(Constants().KEY_RECEIVER_ID,receiverid)
        map.put(Constants().KEY_MESSAGE,binding.msgTextEt.text?.trim().toString())
        map.put(Constants().KEY_TIMESTAMP, Date())
        db.collection(Constants().KEY_COLLECTION_CHAT).add(map)
        if(conversionid != null){
            updateConversion(binding.msgTextEt.text?.trim().toString())
        }else{
            var conversion = HashMap<String,Any>()
            conversion.put(Constants().KEY_SENDER_ID,auth.uid.toString())
            conversion.put(Constants().KEY_RECEIVER_ID,receiverid)
            conversion.put(Constants().KEY_SENDER_NAME,sname)
            conversion.put(Constants().KEY_SENDER_IMAGE,simage)
            conversion.put(Constants().KEY_RECEIVER_IMAGE,rimage)
            conversion.put(Constants().KEY_RECEIVER_NAME,rname)
            conversion.put(Constants().KEY_LASTMESSAGE,binding.msgTextEt.text?.trim().toString())
            conversion.put(Constants().KEY_TIMESTAMP,Date())
            addConversion(conversion)
        }
        binding.msgTextEt.text = null
    }
    fun readableDate(date:Date):String{
        return SimpleDateFormat("hh:mm a - MMM dd, yyyy",Locale.getDefault()).format(date)
    }
}
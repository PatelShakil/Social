package com.bcgroup.social_media.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.ActivitySocialMediaBinding
import com.bcgroup.social_media.adapters.SendPostAdapter
import com.bcgroup.social_media.fragments.*
import com.bcgroup.social_media.models.UserModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.*

class SocialMediaActivity : BaseActivity() {
    private lateinit var binding: ActivitySocialMediaBinding
    var userName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.socialToolbar)
        dbbase.collection(Constants().KEY_COLLECTION_USERS)
            .document(FirebaseAuth.getInstance().uid.toString())
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    binding.curUserUsername.text = it.getString("username")
                    binding.curUserName.text = it.getString("name")
                    this.userName = it.getString("name").toString()
                    Glide.with(applicationContext)
                        .load(it.getString("profile_pic"))
                        .placeholder(R.drawable.logoround)
                        .into(binding.curUserProfileIv)
                }
            }
        binding.navigation.selectedItemId = R.id.social_media_home
        supportFragmentManager.beginTransaction().replace(R.id.main_container,HomeFragment(),"home").addToBackStack("home").commit()
        if (intent.extras?.getString("l") == "l"){
            var link = intent.extras?.getString("link")
            var sheet : BottomSheetDialog
            sheet = BottomSheetDialog(this)
            sheet.setContentView(R.layout.send_layout)
            var rv = sheet.findViewById<RecyclerView>(R.id.send_users_rv)
            sheet.findViewById<TextView>(R.id.send_post_caption)?.text = link
            var users_list = ArrayList<UserModel>()
            FirebaseFirestore.getInstance().collection(Constants().KEY_COLLECTION_USERS)
                .whereNotEqualTo("uid", FirebaseAuth.getInstance().uid.toString())
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
                        var users_adapter = SendPostAdapter(users_list,this,link!!,"link")
                        rv?.adapter = users_adapter
                        users_adapter.notifyDataSetChanged()
                    }
                }
            sheet.show()
        }
        else if (intent.extras?.getString("location") == "chat"){
            var fragment = ViewUserProfileFragment()
            var bundle = Bundle()
            bundle.putString("uid",intent.extras?.getString("uid"))
            fragment.arguments = bundle
//                    (BottomNavigationView(context?.applicationContext!!)).findViewById<BottomNavigationView>(R.id.navigation).visibility = View.GONE
            supportFragmentManager.beginTransaction().replace(R.id.main_container,fragment).addToBackStack("view_user").commit()
        }else if (intent.extras?.getString("location") == "post"){
            var fg = PostViewFragment()
            var bundle = Bundle()
            bundle.putString("post_id",intent.extras?.getString("post_id"))
            fg.arguments = bundle
//                    (BottomNavigationView(context?.applicationContext!!)).findViewById<BottomNavigationView>(R.id.navigation).visibility = View.GONE
            supportFragmentManager.beginTransaction().replace(R.id.main_container,fg).addToBackStack("view_user").commit()
        }
        binding.navigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.social_media_search -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, SearchFragment(),"search").addToBackStack("search").commit()
                }
                R.id.social_media_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, HomeFragment(),"home").addToBackStack("home").commit()
                }
                R.id.social_media_chats -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, UsersFragment(),"chats").addToBackStack("chats").commit()
                }
                R.id.social_media_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ProfileFragment(),"profile").addToBackStack("profile").commit()
                }
            }
            true
        }

//        binding.navigationNv.add(MeowBottomNavigation.Model(1,R.drawable.live))
//        binding.navigationNv.add(MeowBottomNavigation.Model(2,R.drawable.search_icon))
//        binding.navigationNv.add(MeowBottomNavigation.Model(3,R.drawable.home_icon))
//        binding.navigationNv.add(MeowBottomNavigation.Model(4,R.drawable.chats_icon))
//        binding.navigationNv.add(MeowBottomNavigation.Model(5,R.drawable.person_icon))
//
//        binding.navigationNv.setOnClickMenuListener {
//            when (it.id){
//                1 -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.main_container, ViewUserProfileFragment()).commit()
//                }
//                2 -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.main_container, SearchFragment()).commit()
//                }
//                3 -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.main_container, HomeFragment()).commit()
//                }
//                4 -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.main_container, ChatsFragment()).commit()
//                }
//                5 -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.main_container, ProfileFragment()).commit()
//                }
//            }
//        }
//        binding.navigationNv.show(3,true)

//        if (supportFragmentManager.findFragmentByTag("home")?.isVisible!!) {
//            binding.navigation.selectedItemId = R.id.social_media_home
//        }
//        else if (supportFragmentManager.findFragmentByTag("chats")?.isVisible!!){
//            binding.navigation.selectedItemId = R.id.social_media_chats
//        }
//        else if (supportFragmentManager.findFragmentByTag("profile")?.isVisible!!){
//            binding.navigation.selectedItemId = R.id.social_media_profile
//        }
//        else if (supportFragmentManager.findFragmentByTag("live")?.isVisible!!){
//            binding.navigation.selectedItemId = R.id.social_media_live
//        }
//        else if (supportFragmentManager.findFragmentByTag("search")?.isVisible!!){
//            binding.navigation.selectedItemId = R.id.social_media_search
//        }
        binding.assistant.setOnClickListener {
            var sheet = BottomSheetDialog(this)
            sheet.setContentView(R.layout.assistant_layout)
            sheet.show()
            if (checkAudioPermission())
                startSpeechToText(sheet)
        }


    }

    private fun startSpeechToText(sheet : BottomSheetDialog) {
//        ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService")

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle?) {
            }
            override fun onBeginningOfSpeech() {
                Toast.makeText(this@SocialMediaActivity,"Listening...",Toast.LENGTH_SHORT).show()
            }
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray?) {}
            override fun onEndOfSpeech() {
                sheet.findViewById<ImageButton>(R.id.retry)?.setOnClickListener {
                    startSpeechToText(sheet)
                }
            }
            override fun onError(i: Int) {
                Toast.makeText(this@SocialMediaActivity,i.toString(),Toast.LENGTH_SHORT).show()
                sheet.findViewById<ImageButton>(R.id.retry)?.setOnClickListener {
                    startSpeechToText(sheet)
                }
            }
            override fun onResults(bundle: Bundle) {
                val result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (result != null) {
                    // result[0] will give the output of speech
                    sheet.findViewById<ImageButton>(R.id.retry)?.setOnClickListener {
                        startSpeechToText(sheet)
                    }
                    var q = Volley.newRequestQueue(this@SocialMediaActivity)
                    var url = "http://api.brainshop.ai/get?bid=171183&key=JFisLuBJdyaNKUr7&uid=[$userName]&msg=[${result[0]}]"
                    var jor = JsonObjectRequest(Request.Method.GET,url,null
                        ,{
                            var tts : TextToSpeech? = null
                            tts = TextToSpeech(this@SocialMediaActivity
                            ) { i->
                                if(i == TextToSpeech.SUCCESS){
                                    GlobalScope.async {
                                        if(analyzeText(result[0].toString().lowercase()))
                                            tts?.let { it1 -> speak("Lo mene kar diya", it1) }
                                        else{
                                            tts?.let { it1 -> speak(it.getString("cnt"), it1) }
                                        }
                                    }
                                }
                            }
                        },{
                            Toast.makeText(this@SocialMediaActivity,it.localizedMessage,Toast.LENGTH_SHORT).show()
                        }
                    )
                    q.add(jor)
                    sheet.hide()
                }
            }
            override fun onPartialResults(bundle: Bundle) {
                val result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (result != null) {
                    // result[0] will give the output of speech
                    Toast.makeText(this@SocialMediaActivity,result[0].toString(),Toast.LENGTH_SHORT).show()
                }
            }
            override fun onEvent(i: Int, bundle: Bundle?) {}
        })
        // starts listening ...
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    private fun speak(string: String,tts:TextToSpeech) {
        tts.speak(string,TextToSpeech.QUEUE_FLUSH,null,"")
    }

    private suspend fun analyzeText(cmd: String):Boolean {
        var action = checkAction(cmd)
        var cf = cmd.replace(action,"")
        Log.d(action,cf)
        if (action == "open") {
            actionOpen(cf.replace(" ", ""))
            return true
        } else
            return false
    }

    private fun actionOpen(cmd: String) {
        val openList : Array<String> = arrayOf("search","friends","home","profile")
        if (cmd.contains("search"))
            openFragment(SearchFragment())
        else if (cmd.contains("home"))
            openFragment(HomeFragment())
        else if (cmd.contains("friends"))
            openFragment(UsersFragment())
        else if (cmd.contains("profile"))
            openFragment(ProfileFragment())
        else
            Toast.makeText(this,"Try again....",Toast.LENGTH_SHORT).show()

    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.main_container,fragment).addToBackStack("from_search").commit()
    }

    private fun checkAction(cmd: String): String {
        val cmdList:Array<String> = arrayOf("open","message")
        var c = ""
        if (cmd.contains("open")){
            c = "open"
        }else{
            c = "default"
        }
        return c
    }

    private fun checkAudioPermission():Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // M = 23
            if(ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                // this will open settings which asks for permission
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO),1)
                Toast.makeText(this, "Allow Microphone Permission", Toast.LENGTH_SHORT).show()
            }
        }
        return true

    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else
            finishAffinity()
        }

    override fun onStart() {
        super.onStart()
    }
}
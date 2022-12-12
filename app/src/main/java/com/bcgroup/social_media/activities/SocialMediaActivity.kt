package com.bcgroup.social_media.activities

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bcgroup.R
import com.bcgroup.classes.Constants
import com.bcgroup.databinding.ActivitySocialMediaBinding
import com.bcgroup.social_media.adapters.SendPostAdapter
import com.bcgroup.social_media.fragments.HomeFragment
import com.bcgroup.social_media.fragments.ProfileFragment
import com.bcgroup.social_media.fragments.SearchFragment
import com.bcgroup.social_media.fragments.UsersFragment
import com.bcgroup.social_media.models.UserModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SocialMediaActivity : BaseActivity() {
    private lateinit var binding: ActivitySocialMediaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navigation.selectedItemId = R.id.social_media_home
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
        supportFragmentManager.beginTransaction().replace(R.id.main_container,HomeFragment(),"home").addToBackStack("home").commit()
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
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else
            finishAffinity()
        }
}
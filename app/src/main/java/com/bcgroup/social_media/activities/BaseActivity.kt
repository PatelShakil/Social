package com.bcgroup.social_media.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.bcgroup.classes.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {
    var dbbase = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onPause() {
        super.onPause()
        dbbase.collection(Constants().KEY_COLLECTION_USERS)
            .document(FirebaseAuth.getInstance().uid.toString()).update(Constants().KEY_USERSTATUS,0)
    }

    override fun onResume() {
        super.onResume()
        dbbase.collection(Constants().KEY_COLLECTION_USERS)
            .document(FirebaseAuth.getInstance().uid.toString()).update(Constants().KEY_USERSTATUS,1)
    }

}
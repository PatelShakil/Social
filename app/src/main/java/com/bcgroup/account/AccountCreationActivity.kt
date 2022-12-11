package com.bcgroup.account

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bcgroup.databinding.ActivityAccountCreationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.bcgroup.social_media.SocialMediaActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class AccountCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountCreationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database:FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var db:FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseDatabase.getInstance().reference.keepSynced(true)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()
        try{
            if (intent.action == Intent.ACTION_SEND && intent.type == Intent.EXTRA_TEXT){
                var msg = intent.getStringExtra(Intent.EXTRA_TEXT)
                Toast.makeText(this,msg.toString(),Toast.LENGTH_SHORT).show()
            }
        }catch(e :Exception){
            Toast.makeText(this,e.message.toString(),Toast.LENGTH_SHORT).show()
        }

        if(auth.uid.toString() == auth.currentUser?.uid){
            startActivity(Intent(this,SocialMediaActivity::class.java))
        }
        binding.loginAlreadyLoginTv.setOnClickListener {
            binding.loginLayout.visibility = View.GONE
            binding.signupLayout.visibility = View.VISIBLE
        }
        binding.signupAlreadySignupTv.setOnClickListener {
            binding.loginLayout.visibility = View.VISIBLE
            binding.signupLayout.visibility = View.GONE
        }
        binding.signupSignupBtn.setOnClickListener {
            ac_signup(binding.signupUsernameEt.text.toString(),binding.signupEmailEt.text.toString(),binding.signupPasswordConfirmEt.text.toString())

        }
        binding.loginLoginBtn.setOnClickListener {
            if (binding.loginEmailEt.text.isNotEmpty()&&binding.loginPasswordEt.text.isNotEmpty()){
                ac_login(binding.loginEmailEt.text.toString(),binding.loginPasswordEt.text.toString())
            }
        }
    }
    private fun ac_login(email:String,password:String){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    startActivity(Intent(this,SocialMediaActivity::class.java))
                    Toast.makeText(this,"Welcome back to Sastagram",Toast.LENGTH_SHORT).show()
                }
                else
                    Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()
            }
    }
    private fun ac_signup(username:String,email:String,password:String){
        var check = false
        db.collection(Constants().KEY_COLLECTION_USERS).orderBy("username")
            .get()
            .addOnSuccessListener {
                if(!it.isEmpty){
                    for (i in it.documents){
                        if (i["username"] != username){
                            check = true
                            break
                        }
                    }
                }
                if (check) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                var user = HashMap<String, Any>()
                                user.put(
                                    Constants().KEY_NAME,
                                    binding.signupUsernameEt.text.trim().toString()
                                )
                                user.put(
                                    Constants().KEY_USERNAME,
                                    binding.signupUsernameEt.text.trim().toString()
                                )
                                user.put(
                                    Constants().KEY_EMAIL,
                                    binding.signupEmailEt.text.trim().toString()
                                )
                                user.put(
                                    Constants().KEY_PASSWORD,
                                    binding.signupPasswordConfirmEt.text.trim().toString()
                                )
                                user.put(Constants().KEY_USER_ID, auth.uid.toString())
                                FirebaseFirestore.getInstance().collection(Constants().KEY_COLLECTION_USERS)
                                    .document(auth.uid.toString())
                                    .set(user)
                                    .addOnSuccessListener {
                                        startActivity(Intent(this, SocialMediaActivity::class.java))
                                        Toast.makeText(
                                            this,
                                            "Welcome back to Sastagram",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                                    }

                            } else {
                                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                else {
                    binding.signupEmailEt.error = "Enter Unique Username"
                    Toast.makeText(this, "Enter Unique username", Toast.LENGTH_SHORT).show()
                }
            }

    }

    override fun onStart() {
        super.onStart()
        gettoken()
    }
    fun updatetoken(token:String){
        db.collection(Constants().KEY_COLLECTION_USERS)
            .document(auth.uid.toString())
            .update(Constants().KEY_FCM_TOKEN,token)
    }
    fun gettoken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            updatetoken(it.toString())
        }
    }
}

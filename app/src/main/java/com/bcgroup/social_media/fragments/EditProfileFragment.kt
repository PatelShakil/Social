package com.bcgroup.social_media.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import com.bcgroup.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.bcgroup.R
import com.bcgroup.account.Constants
import java.util.*

class EditProfileFragment : Fragment() {
    lateinit var binding: FragmentEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var db:FirebaseFirestore
    var profile_pic_url:Uri = Uri.parse(" ")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()

        db.collection(Constants().KEY_COLLECTION_USERS)
            .document(auth.uid.toString())
            .get()
            .addOnSuccessListener {
                if(it.exists()){
                    binding.username.text = it["username"].toString()
                    binding.editBio.setText(it["bio"].toString())
                    binding.editName.setText(it["name"].toString())
                    binding.editDob.setText(it["dob"].toString())
                    binding.editEmail.text = it["email"].toString()
                    binding.editPhone.setText(it["phone"].toString())
                    Glide.with(context?.applicationContext!!)
                        .load(it["profile_pic"].toString())
                        .placeholder(R.drawable.profile_icon)
                        .into(binding.editProfilePic)
                    binding.editName.doOnTextChanged { text, start, before, count ->
                        if(text.toString()!=it["name"].toString()){
                            binding.editNameCheck.visibility = View.VISIBLE
                        }
                        else
                            binding.editNameCheck.visibility = View.GONE
                    }
                    binding.editBio.doOnTextChanged { text, start, before, count ->
                        if(text.toString()!=it["bio"].toString()){
                            binding.editBioCheck.visibility = View.VISIBLE
                        }
                        else
                            binding.editBioCheck.visibility = View.GONE
                    }
                    binding.editDob.doOnTextChanged { text, start, before, count ->
                        if(text.toString()!=it["dob"].toString()){
                            binding.editDobCheck.visibility = View.VISIBLE
                        }
                        else
                            binding.editDobCheck.visibility = View.GONE
                    }
                    binding.editPhone.doOnTextChanged { text, start, before, count ->
                        if(text.toString()!=it["phone"].toString()){
                            binding.editPhoneCheck.visibility = View.VISIBLE
                        }
                        else
                            binding.editPhoneCheck.visibility = View.GONE
                    }
                    binding.editNameCheck.setOnClickListener {
                        if (binding.editName.text.toString().isNotEmpty()){
                            DataUpdater("name",binding.editName.text.toString())
                            binding.editNameCheck.visibility =View.GONE
                        }
                    }
                    binding.editBioCheck.setOnClickListener {
                        if (binding.editBio.text.toString().isNotEmpty()){
                            DataUpdater("bio",binding.editBio.text.toString())
                            binding.editBioCheck.visibility =View.GONE
                        }
                    }
                    binding.editDobCheck.setOnClickListener {
                        if (binding.editDob.text.toString().isNotEmpty()){
                            DataUpdater("dob",binding.editDob.text.toString())
                            binding.editDobCheck.visibility =View.GONE
                        }
                    }
                    binding.editPhoneCheck.setOnClickListener {
                        if (binding.editPhone.text.toString().isNotEmpty()){
                            DataUpdater("phone",binding.editPhone.text.toString())
                            binding.editPhoneCheck.visibility =View.GONE
                        }
                    }
                }
            }
        binding.editProfilePic.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent,75)
            binding.editProfilePicBtn.visibility = View.VISIBLE
        }
        binding.editProfilePicBtn.setOnClickListener {
            if(profile_pic_url != Uri.parse(" ")) {
                var sr = storage.reference.child("profiles/" + auth.uid + Date().date)
                sr.putFile(profile_pic_url)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            sr.downloadUrl.addOnSuccessListener {
                                DataUpdater("profile_pic", it.toString())
                                Toast.makeText(
                                    context,
                                    "Profile pic Uploaded successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
            else
                Toast.makeText(context,"Please Select Profile picture",Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }
    fun DataUpdater(data: String,value:String){
       db.collection(Constants().KEY_COLLECTION_USERS)
           .document(auth.uid.toString())
           .update(data,value)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data!=null){
            binding.editProfilePic.setImageURI(data.data)
            profile_pic_url = data.data!!
        }
        else
            Toast.makeText(context,"Please Select Profile picture",Toast.LENGTH_SHORT).show()
    }
}
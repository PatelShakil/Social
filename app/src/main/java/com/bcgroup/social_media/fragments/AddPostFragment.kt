package com.bcgroup.social_media.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bcgroup.R
import com.bcgroup.databinding.FragmentAddPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.bcgroup.account.Constants
import java.util.*
import com.bcgroup.social_media.models.PostModel

class AddPostFragment : Fragment() {
    lateinit var binding:FragmentAddPostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var encodedpost:String
    lateinit var type:String
    lateinit var video_url:Uri
    var vr = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPostBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val intent = Intent()
        intent.type = "image/* video/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(intent, 75)
        binding.change.setOnClickListener{
            startActivityForResult(intent,75)
        }
        binding.postUploadBtn.setOnClickListener {
            if (binding.postCaption.text?.isNotEmpty()!!){
                if (type == "iv") {
                    if (encodedpost.isNotEmpty()) {
                        database.reference.child("social_media").child("posts")
                            .child(database.reference.push().key.toString())
                            .setValue(
                                PostModel(
                                    binding.postCaption.text.toString(),
                                    auth.uid.toString(),
                                    encodedpost,
                                    Date().time,
                                    0
                                )
                            ).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    database.reference.child("social_media")
                                        .child("posts")
                                        .addValueEventListener(object :ValueEventListener{
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()){
                                                    for (i in snapshot.children){
                                                       if(i.child("post_url").value == encodedpost){
                                                           database.reference.child("social_media")
                                                               .child("posts")
                                                               .child(i.key.toString())
                                                               .child("post_id")
                                                               .setValue(i.key)
                                                               .addOnSuccessListener {
                                                               }
                                                       }
                                                    }
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
                                    Toast.makeText(context, "Post uploaded", Toast.LENGTH_SHORT)
                                        .show()
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.main_container, ProfileFragment(), "profile")
                                        .addToBackStack("profile").commit()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Please select post", Toast.LENGTH_SHORT).show()
                    }
                }else if (type == "vv"){
                    if(video_url.toString().isNotEmpty()) {
                        var pd = ProgressDialog(context)
                        pd.setTitle("Your Post Uploading...")
                        pd.show()
                        var rf =
                            FirebaseStorage.getInstance().reference.child("post/" + auth.uid + Date().toString() + ".mp4")
                        rf.putFile(video_url)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    rf.downloadUrl.addOnSuccessListener {
                                        vr = it.toString()
                                        database.reference.child("social_media").child("posts")
                                            .child(database.reference.push().key.toString())
                                            .setValue(
                                                PostModel(
                                                    binding.postCaption.text.toString(),
                                                    auth.uid.toString(),
                                                    it.toString(),
                                                    Date().time,
                                                    0
                                                )
                                            )
                                            .addOnSuccessListener {
                                                database.reference.child("social_media")
                                                    .child("posts")
                                                    .addValueEventListener(object :ValueEventListener{
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            if (snapshot.exists()){
                                                                for (i in snapshot.children){
                                                                    if(i.child("post_url").value == vr){
                                                                        database.reference.child("social_media")
                                                                            .child("posts")
                                                                            .child(i.key.toString())
                                                                            .child("post_id")
                                                                            .setValue(i.key)
                                                                            .addOnSuccessListener {
                                                                            }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        override fun onCancelled(error: DatabaseError) {
                                                        }
                                                    })
                                                Toast.makeText(
                                                    context,
                                                    "Post uploaded",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                                pd.dismiss()
                                                parentFragmentManager.beginTransaction()
                                                    .replace(
                                                        R.id.main_container,
                                                        ProfileFragment(),
                                                        "profile"
                                                    )
                                                    .addToBackStack("profile").commit()
                                            }

                                    }
                                }
                            }
                            .addOnProgressListener{
                                var b = it.bytesTransferred / 1000
                                var t = it.totalByteCount / 1000
                                pd.setMessage("$b/$t KB")
                                pd.setCancelable(false)
                            }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data!=null){
            if (data.data.toString().contains("JPEG") || data.data.toString().lowercase().contains("png") || data.data.toString().lowercase().contains("jpg")) {
                try {
                    var inputStream = context?.contentResolver?.openInputStream(data.data!!)
                    var bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.addPostPic.setImageBitmap(bitmap)
                    encodedpost = Constants().encodeImage(bitmap)
                    binding.addPostPic.visibility = View.VISIBLE
                    binding.addPostVid.visibility = View.GONE
                    type = "iv"
                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }else if (
                data.data.toString().contains("mp4") ){
                type = "vv"
                binding.addPostVid.visibility = View.VISIBLE
                binding.addPostPic.visibility = View.GONE
                binding.addPostVid.setVideoURI(data.data)
                binding.addPostVid.setMediaController(MediaController(context))
                binding.addPostVid.drawingTime
                binding.addPostVid.duration
                binding.addPostVid.start()
                video_url = data.data!!
            }else{
                Toast.makeText(context,"Error Occurred",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
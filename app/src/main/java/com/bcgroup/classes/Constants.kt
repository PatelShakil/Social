package com.bcgroup.classes

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

class Constants {
    var KEY_USERNAME =  "username"
    var KEY_COLLECTION_USERS = "users"
    var KEY_NAME ="name"
    var KEY_EMAIL = "email"
    var KEY_PASSWORD = "password"
    var KEY_USER_ID = "uid"
    var KEY_FCM_TOKEN = "token"
    var KEY_SENDER_ID = "senderid"
    var KEY_RECEIVER_ID = "receiverid"
    var KEY_COLLECTION_CHAT = "chat"
    var KEY_MESSAGE = "message"
    var KEY_TIMESTAMP = "timestamp"
    var KEY_COLLECTION_CONVERSIONS = "conversions"
    var KEY_SENDER_IMAGE = "senderprofile"
    var KEY_SENDER_NAME = "sendername"
    var KEY_RECEIVER_IMAGE = "receiverprofile"
    var KEY_RECEIVER_NAME = "receivername"
    var KEY_LASTMESSAGE = "lastmessage"
    var KEY_USERSTATUS =  "status"
    var KEY_FOLLOWERS = "followers"
    var KEY_FOLLOWING = "following"

    fun encodeImage(bitmap: Bitmap):String{
        var pwidth = bitmap.width
        var pheight = bitmap.height
        var pbitmap = Bitmap.createScaledBitmap(bitmap,pwidth,pheight,false)
        var byteArrayOutputStream = ByteArrayOutputStream()
        pbitmap.compress(Bitmap.CompressFormat.JPEG,80,byteArrayOutputStream)
        var bytes=byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes,Base64.DEFAULT)
    }
    fun decodeImage(postimg:String):Bitmap{
        var bytes = Base64.decode(postimg,Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }
}
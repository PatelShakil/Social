package com.bcgroup.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.bcgroup.R
import com.bcgroup.social_media.activities.ChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class FirebaseService : FirebaseMessagingService() {
    var CHANNEL_ID = "channel_id"
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("Msg", "recieved")
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("uid", message.data["uid"])
        intent.putExtra("location","notification")
        val manager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random().nextInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(manager)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        var intent1: PendingIntent? = null
        intent1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivities(
                this,
                0,
                arrayOf<Intent>(intent),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getActivities(
                this,
                0,
                arrayOf<Intent>(intent),
                PendingIntent.FLAG_ONE_SHOT
            )
        }
        var notification: Notification? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(message.getData().get("title"))
                .setContentText(message.getData().get("message"))
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(intent1)
                .build()
        }
        manager.notify(notificationId, notification)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(manager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,"${R.string.app_name}",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.setDescription("We notified you or each and every event.")
        channel.enableLights(true)
        channel.setLightColor(Color.GREEN)
        channel.enableVibration(true)
        manager.createNotificationChannel(channel)
    }
}
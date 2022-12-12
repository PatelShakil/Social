package com.bcgroup.notification

object Cons {
    const val BASE_URL = "https://fcm.googleapis.com"
    const val SERVER_KEY = "AAAAmbXktVQ:APA91bHohc5IUw1TlRJzvtx5NFmNgwqFji3vDFV9jmu7FPh_MKOHQtqDDHc4DaXCpUcH-S0z3D6UaAxo0OpWIY2qRYia8mP6O7qqjFnlcYZMTilHeX7U7D8rkCRZSFiNXgwlnG2SJZwR"
    const val CONTENT_TYPE = "application/json"
    const val TOPIC_ALL = "/topics/all"
   private fun sendNotification(notification: PushNotification) {
        ApiUtils.client.sendNotification(notification)
            ?.enqueue(object : retrofit2.Callback<PushNotification?> {
                override fun onResponse(
                    call: retrofit2.Call<PushNotification?>,
                    response: retrofit2.Response<PushNotification?>
                ) {
                    //handle on successfully sending notification
                }

                override fun onFailure(call: retrofit2.Call<PushNotification?>, t: Throwable) {
                  //handle on sending notification failure
                }
            })
    }

    //Push Notification
//    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
//    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

    //Manifest permissions and services
//    <service
//    android:name=".notification.FirebaseService"
//    android:exported="true"
//    android:permission="com.google.android.c2dm.permission.Send">
//    <intent-filter>
//    <action android:name="com.google.firebase.MESSAGING_EVENT" />
//    <action android:name="com.google.android.c2dm.permission.Recieve" />
//    </intent-filter>
//    </service>
//    <uses-permission android:name="com.google.android.c2dm.permission.Reciever" />

}
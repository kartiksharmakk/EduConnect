package com.kartik.tutordashboard.Notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyNotifications: FirebaseMessagingService (){
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG,"From: ${message.from}")
        message.notification?.let {
            Log.d(TAG,"Notification Body: ${it.body}")
        }
    }

    companion object{
        const val TAG = "MyNotification"
    }
}
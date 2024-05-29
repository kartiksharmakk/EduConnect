package com.kartik.tutordashboard.Notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.R

class MyNotifications: FirebaseMessagingService (){
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG,"From: ${message.from}")
        message.notification?.let {
            Log.d(TAG,"No" +
                    "tification Body: ${it.body}")

            it.body?.let { it1 -> sendNotification(it1) }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        updateDeviceToken()

    }

    fun updateDeviceToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful){
                val email = Prefs.getUSerEmailEncoded(this)!!
                val deviceToken = task.result
                Firebase.database.getReference("User Details").child(email).child("deviceToken").setValue(deviceToken)
                Log.d("SignIn","Device Token: $deviceToken")
            }else{
                Log.d("SignIn","Device Token error")
            }
        }
    }

    fun sendNotification(messageBody: String) {

        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle("Notification")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }


    companion object{
        const val TAG = "MyNotification"
    }
}
package com.kartik.tutordashboard.Notifications

import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kartik.tutordashboard.Data.Prefs

class MyNotifications: FirebaseMessagingService (){
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG,"From: ${message.from}")
        message.notification?.let {
            Log.d(TAG,"No" +
                    "tification Body: ${it.body}")
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


    companion object{
        const val TAG = "MyNotification"
    }
}
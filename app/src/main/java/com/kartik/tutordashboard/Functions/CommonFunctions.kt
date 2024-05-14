package com.kartik.tutordashboard.Functions

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import java.util.Calendar

object CommonFunctions {
    private const val PERMISSION_REQUEST_CODE = 1000
    fun getToastShort(context: Context, text: String){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
    fun getToastLong(context: Context, text: String){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun getEditTextLength(editText: EditText): Int{
        return editText.text.toString().trim().length
    }

    fun dropNavBar(window: WindowManager){

    }

    fun handleKeyboardInFragment(fragment: Fragment, scrollView: NestedScrollView, ){
        val activity = fragment.requireActivity()
        val softInputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val rootView = fragment.requireView().rootView

        rootView.viewTreeObserver.addOnGlobalLayoutListener (object: ViewTreeObserver.OnGlobalLayoutListener{
            private var rootHeight = 0

            override fun onGlobalLayout() {
                val viewHeight = rootView.height
                if(rootHeight == 0){
                    rootHeight = viewHeight
                }

                val heightDifference = rootHeight - viewHeight

                if(heightDifference > 100){
                    softInputManager.showSoftInput(fragment.requireView(), InputMethodManager.SHOW_FORCED)
                    scrollView.smoothScrollTo(0, heightDifference)
                }else{
                    scrollView.smoothScrollTo(0,0)
                }
            }

        } )
    }

    fun showDatePicker(context: Context,editText: EditText){
        val calendar: Calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        var datePickerDialog = DatePickerDialog(context,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val date = "$year-${month + 1}-$dayOfMonth"
                editText.setText(date)
            },year,month,day)
        datePickerDialog.show()
    }

    fun loadFragmentFromActivity(frameLayout: Int, fragment: Fragment, fragmentManager: FragmentManager){
        val frag = fragmentManager.beginTransaction()
        frag.replace(frameLayout, fragment)
        frag.commit()
    }
    fun loadFragmentFromFragment(frameLayout: Int,  fragment: Fragment, activity: FragmentActivity){
        val frag = activity.supportFragmentManager.beginTransaction()
        frag.replace(frameLayout, fragment)
        frag.commit()

    }

    fun loadFragmentFromFragment(frameLayout: Int, fragment: Fragment, activity: FragmentActivity, bundle: Bundle){
        val frag = activity.supportFragmentManager.beginTransaction()
        fragment.arguments = bundle
        frag.replace(frameLayout, fragment)
        frag.commit()
    }


    fun getPermissions(activity: FragmentActivity, requiredPermissions: Array<String>) {
        val missingPermissions = ArrayList<String>()

        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    fun onRequestPermissionsResult(activity: FragmentActivity, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }

            if (!allPermissionsGranted) {
                showPermissionDialog(activity, permissions)
            }
        }
    }

    private fun showPermissionDialog(activity: FragmentActivity, permissions: Array<out String>) {
        for (permission in permissions) {
            val title: String
            val message: String

            when (permission) {
                Manifest.permission.INTERNET -> {
                    title = "Internet Permissions Required"
                    message = "This app requires internet permissions to function properly"
                }
                Manifest.permission.ACCESS_NETWORK_STATE -> {
                    title = "Network State Permissions Required"
                    message = "This app requires network state permission to function properly"
                }
                Manifest.permission.POST_NOTIFICATIONS -> {
                    title = "Notification Permission Required"
                    message = "This app requires Notification permission to function properly."
                }
                else -> {
                    title = ""
                    message = ""
                }
            }

            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Grant") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = uri
                    activity.startActivity(intent)

                }
                .setNegativeButton("Cancel") { _, _ ->

                }
                .setCancelable(false)
                .show()
        }
    }

    fun sendNotificationToDevice(deviceToken: String, title: String, message: String){
        try {
            val notification = RemoteMessage.Builder(deviceToken).setMessageId(System.currentTimeMillis().toString())
                .addData("title",title)
                .addData("message",message)
                .build()
            FirebaseMessaging.getInstance().send(notification)
        }catch (e: Exception){
            Log.e("SendNotificationToDevice","Error: ${e.message}")
        }
    }
}


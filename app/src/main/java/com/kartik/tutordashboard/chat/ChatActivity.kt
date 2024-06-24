package com.kartik.tutordashboard.chat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.kartik.tutordashboard.Adapter.QuestionAdapter
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.databinding.ActivityChatBinding
import java.io.ByteArrayOutputStream

class ChatActivity : AppCompatActivity() , FriendlyMessageAdapter.onclickListner {
    private lateinit var binding: ActivityChatBinding
    private lateinit var manager: LinearLayoutManager
    var finalUrl: String = ""
    var finalName: String = ""


//    private val openDocument = registerForActivityResult(MyOpenDocumentContract()) { uri ->
//        if (uri != null) {
//            pickProfilePhoto(uri)
//        }
//    }


    // TODO: implement Firebase instance variables
    // Firebase instance variables
    private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseDatabase
    private lateinit var adapter: FriendlyMessageAdapter
    lateinit var databaseReferenceUserDetails: DatabaseReference
    var email:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This codelab uses View Binding
        // See: https://developer.android.com/topic/libraries/view-binding
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
/*
        if (BuildConfig.DEBUG) {
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.storage.useEmulator("10.0.2.2", 9199)
        }
*/
        // Initialize Realtime Database and FirebaseRecyclerAdapter
        // TODO: implement
        auth = FirebaseAuth.getInstance()
        db = Firebase.database
        databaseReferenceUserDetails = db.getReference("User Details")
        val messagesRef = db.reference.child(MESSAGES_CHILD)
        getUserName()
        getPhotoUrl()
        email = Prefs.getUSerEmailEncoded(this)

        // The FirebaseRecyclerAdapter class and options come from the FirebaseUI library
        // See: https://github.com/firebase/FirebaseUI-Android
        val options = FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                .setQuery(messagesRef, FriendlyMessage::class.java)
                .build()
        adapter = FriendlyMessageAdapter(options, finalName, email, this)
        binding.progressBar.visibility = ProgressBar.INVISIBLE
        manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        binding.messageRecyclerView.layoutManager = manager
        binding.messageRecyclerView.adapter = adapter

        // Scroll down when a new message arrives
        // See MyScrollToBottomObserver for details

        adapter.registerAdapterDataObserver(
                MyScrollToBottomObserver(binding.messageRecyclerView, adapter, manager)
        )


        // Disable the send button when there's no text in the input field
        // See MyButtonObserver for details
        binding.messageEditText.addTextChangedListener(MyButtonObserver(binding.sendButton))
        // When the send button is clicked, send a text message
        binding.sendButton.setOnClickListener {
            Log.d("final",finalName)
            Log.d("final",finalUrl)
            val friendlyMessage = FriendlyMessage(
                    binding.messageEditText.text.toString(),
                    finalName,
                    finalUrl,
                    null /* no image */,
                    email
            )
            db.reference.child(MESSAGES_CHILD).push().setValue(friendlyMessage)
            binding.messageEditText.setText("")
        }


        // When the image button is clicked, launch the image picker
        binding.addMessageImageView.setOnClickListener {
           //openDocument.launch(arrayOf("image/*"))
            pickProfilePhoto()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.messageRecyclerView.recycledViewPool.clear()
        adapter.startListening()
    }


    override fun onDestroy() {
        adapter.stopListening()
        super.onDestroy()
    }
    /*
    public override fun onPause() {
        adapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        adapter.startListening()
    }

     */

    private fun pickProfilePhoto(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type ="image/*"
        startActivityForResult(intent, ChatActivity.PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("ProfileFragment", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ChatActivity.PICK_IMAGE_REQUEST_CODE) {
                Log.d("StudentProfileFragment", "Picked image successfully")
                data?.data?.let { uri ->
                    uploadChatPhoto(uri)
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("StudentProfileFragment", "Activity result cancelled")
        }
    }

    private fun uploadChatPhoto(imageUri: Uri){
        try{
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val data = outputStream.toByteArray()
            val email = Prefs.getUSerEmailEncoded(this)
            val fileName ="${getRandomString(10)}.jpg"//pending
            val profilePhotoRef = FirebaseStorage.getInstance().reference.child("message_photos/$fileName")
            val uploadTask = profilePhotoRef.putBytes(data)
            uploadTask.addOnSuccessListener {taskSnapshot->
                profilePhotoRef.downloadUrl.addOnSuccessListener { uri->
                    //SharedPrefs
                    updateImageUriInDatabaseChat(uri.toString())

                }
            }.addOnFailureListener{e ->
                Log.e("ProfileFragment", "Error uploading profile photo: ${e.message}")
            }
        }catch (e: Exception){
            Log.e("ProfileFragment", "Error uploading profile photo: ${e.message}")
        }
    }

    private fun updateImageUriInDatabaseChat(imageUri: String){

        // TODO: implement
        Log.d(TAG, "Uri: $imageUri")
        lateinit var tempMessage:FriendlyMessage
        val user = auth.currentUser
        if(imageUri != null) {
            tempMessage = FriendlyMessage(null, finalName, finalUrl, imageUri.toString(), email)
        }else{
            tempMessage = FriendlyMessage(null, finalName, finalUrl, LOADING_IMAGE_URL, email)

        }


        db.reference
            .child(MESSAGES_CHILD)
            .push()
            .setValue(
                tempMessage,
                DatabaseReference.CompletionListener { databaseError, databaseReference ->
                    if (databaseError != null) {
                        Log.w(
                            TAG, "Unable to write message to database.",
                            databaseError.toException()
                        )
                        return@CompletionListener
                    }
                })
    }

    private fun getRandomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
    private fun getPhotoUrl() {
        val email = Prefs.getUSerEmailEncoded(this)
        val dbRef = databaseReferenceUserDetails.child(email!!).child("image")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val imageUri = dataSnapshot.getValue(String::class.java)
                imageUri?.let {
                    val img = FirebaseStorage.getInstance().getReferenceFromUrl(imageUri)
                    img.downloadUrl.addOnSuccessListener { uri ->
                        finalUrl = uri.toString()
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    "ChatActivity",
                    "Error fetching image URI from database: ${databaseError.message}"
                )
            }
        })
    }

    private fun getUserName() {
        val email = Prefs.getUSerEmailEncoded(this)
        val dbRef = databaseReferenceUserDetails.child(email!!).child("name")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                finalName = dataSnapshot.getValue(String::class.java).toString()
                if (finalName == null) {
                    finalName = ANONYMOUS
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    "ChatActivity",
                    "Error fetching image URI from database: ${databaseError.message}"
                )
            }
        })

    }


    companion object {
        private const val TAG = "ChatActivity"
        const val MESSAGES_CHILD = "messages"
        const val ANONYMOUS = "anonymous"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
        private const val PICK_IMAGE_REQUEST_CODE = 4002
    }

    override fun onClickImage(imageUri: String?) {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(Uri.parse(imageUri), "image/*")
        }
        startActivity(intent)
    }
}

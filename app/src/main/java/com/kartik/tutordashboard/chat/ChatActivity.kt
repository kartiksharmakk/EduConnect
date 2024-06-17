package com.kartik.tutordashboard.chat

import android.net.Uri
import android.os.Bundle
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
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var manager: LinearLayoutManager
    var finalUrl: String = ""
    var finalName: String = ""


    private val openDocument = registerForActivityResult(MyOpenDocumentContract()) { uri ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }


    // TODO: implement Firebase instance variables
    // Firebase instance variables
    private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseDatabase
    private lateinit var adapter: FriendlyMessageAdapter
    lateinit var databaseReferenceUserDetails: DatabaseReference

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
        // The FirebaseRecyclerAdapter class and options come from the FirebaseUI library
        // See: https://github.com/firebase/FirebaseUI-Android
        val options = FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                .setQuery(messagesRef, FriendlyMessage::class.java)
                .build()
        adapter = FriendlyMessageAdapter(options, finalName)
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
                    null /* no image */
            )
            db.reference.child(MESSAGES_CHILD).push().setValue(friendlyMessage)
            binding.messageEditText.setText("")
        }


        // When the image button is clicked, launch the image picker
        binding.addMessageImageView.setOnClickListener {
           // openDocument.launch(arrayOf("image/*"))
        }
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onPause() {
        adapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        adapter.startListening()
    }



    private fun onImageSelected(uri: Uri) {
        // TODO: implement
        Log.d(TAG, "Uri: $uri")
        val user = auth.currentUser
        val tempMessage = FriendlyMessage(null, finalName, finalUrl, LOADING_IMAGE_URL)
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

                            // Build a StorageReference and then upload the file
                            val key = databaseReference.key
                            val storageReference = Firebase.storage
                                    .getReference(user!!.uid)
                                    .child(key!!)
                                    .child(uri.lastPathSegment!!)
                            putImageInStorage(storageReference, uri, key)
                        })
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri, key: String?) {
        // Upload the image to Cloud Storage
        // TODO: implement
        // First upload the image to Cloud Storage
        storageReference.putFile(uri)
                .addOnSuccessListener(
                        this
                ) { taskSnapshot -> // After the image loads, get a public downloadUrl for the image
                    // and add it to the message.
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri ->
                                val friendlyMessage =
                                        FriendlyMessage(null, finalName, finalUrl, uri.toString())
                                db.reference
                                        .child(MESSAGES_CHILD)
                                        .child(key!!)
                                        .setValue(friendlyMessage)
                            }
                }
                .addOnFailureListener(this) { e ->
                    Log.w(
                            TAG,
                            "Image upload task was unsuccessful.",
                            e
                    )
                }
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
    }
}

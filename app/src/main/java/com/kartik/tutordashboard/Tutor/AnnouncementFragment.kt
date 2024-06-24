package com.kartik.tutordashboard.Tutor

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.kartik.tutordashboard.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class AnnouncementFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_announcement_teacher, container, false)
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)
        composeView.apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // In Compose world
                MaterialTheme {
                    AddAnnouncementsScreen()
                }
            }
        }
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        /// hide bottom nav here
        (activity as? TutorHome)?.hideBottomNavigation()
    }

    @Composable
    fun AddAnnouncementsScreen() {
        val title = remember { mutableStateOf("") }
        val description = remember { mutableStateOf("") }
        val url = remember { mutableStateOf("") }
        val snackBarMessage = remember { mutableStateOf("") }
        val snackState = remember { SnackbarHostState() }
        val snackScope = rememberCoroutineScope()
        val selectedUri = remember { mutableStateOf<Uri?>(null) }

        val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            // Handle the selected image URI
            if (uri != null) {
                // You can handle the selected image URI here
                snackBarMessage.value = "Photo selected: $uri"
                uploadPhoto(uri,selectedUri)

            } else {
                snackBarMessage.value = "No photo selected"
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = title.value,
                onValueChange = { title.value = it },
                label = { Text(text = "Title", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = Color.White,
                    cursorColor = Color.White
                ),
                textStyle = TextStyle(color = Color.White)
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = description.value,
                onValueChange = { description.value = it },
                label = { Text(text = "Description", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = Color.White,
                    cursorColor = Color.White
                ),
                textStyle = TextStyle(color = Color.White)
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = url.value,
                onValueChange = { url.value = it },
                label = { Text(text = "URL", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = Color.White,
                    cursorColor = Color.White
                ),
                textStyle = TextStyle(color = Color.White)
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Photo picker button
            IconButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu_camera), // Replace with your photo icon
                    contentDescription = "Pick Photo",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                submitAnnouncement(
                    title.value,
                    description.value,
                    url.value,
                    snackState,
                    snackScope,
                    snackBarMessage,
                    selectedUri
                )
            }) {
                Text(text = "Submit")
            }
        }

        SnackbarHost(
            hostState = snackState,
            snackbar = { data ->
                Snackbar(
                    snackbarData = data,
                    backgroundColor = Color.White,
                    contentColor = Color.Blue
                )
            }
        )

        // Show Snackbar when message is updated
        LaunchedEffect(snackBarMessage.value) {
            if (snackBarMessage.value.isNotEmpty()) {
                snackScope.launch {
                    snackState.showSnackbar(snackBarMessage.value)
                    snackBarMessage.value = "" // Reset message after showing
                }
            }
        }
    }

    fun submitAnnouncement(
        title: String,
        description: String,
        url: String,
        snackState: SnackbarHostState,
        snackScope: CoroutineScope,
        snackBarMessage: MutableState<String>,
        selectedUri: MutableState<Uri?>
    ) {
        if (title.isNotEmpty() && description.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance().reference.child("announcements")
            val newAnnouncement = hashMapOf(
                "timestamp" to ServerValue.TIMESTAMP,
                "title" to title,
                "description" to description,
                "url" to url,
                "image" to selectedUri.value.toString()
            )
            database.push().setValue(newAnnouncement)
                .addOnSuccessListener {
                    // Show the Snackbar on success
                    snackBarMessage.value = "Announcement Created Successfully"
                }
                .addOnFailureListener { exception ->
                    // Handle failure (show error message)
                    Log.w("AddAnnouncement", "Error adding announcement", exception)
                    snackBarMessage.value = "Error adding announcement"
                }
        } else {
            // Handle empty fields (show error message)
            snackBarMessage.value = "Title and Description cannot be empty"
        }
    }

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
    private fun uploadPhoto(imageUri: Uri?, selectedUri: MutableState<Uri?>){
        var finalResultUri: Uri? = null

        try{
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val data = outputStream.toByteArray()
            //val email = Prefs.getUSerEmailEncoded(requireContext())
           // val fileName = "${email}}.jpg"//pending
            val fileName = "${getRandomString(10)}.jpg"


            val PhotoRef = FirebaseStorage.getInstance().reference.child("announcements/$fileName")
            val uploadTask = PhotoRef.putBytes(data)
            uploadTask.addOnSuccessListener {taskSnapshot->
                PhotoRef.downloadUrl.addOnSuccessListener { uri->
                    Log.d("uri",uri.toString())
                    selectedUri.value = uri
                }
            }.addOnFailureListener{e ->
                Log.e("ProfileFragment", "Error uploading profile photo: ${e.message}")
            }
        }catch (e: Exception){
            Log.e("ProfileFragment", "Error uploading profile photo: ${e.message}")
        }
    }

    @Preview
    @Composable
    fun AddAnnouncementsScreenPreview() {
        AddAnnouncementsScreen()
    }
}
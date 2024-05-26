package com.kartik.tutordashboard.Tutor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.kartik.tutordashboard.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


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
    }

    @Composable
    fun AddAnnouncementsScreen() {
        val title = remember { mutableStateOf("") }
        val description = remember { mutableStateOf("") }
        val url = remember { mutableStateOf("") }
        val snackBarMessage = remember { mutableStateOf("") }
        val snackState = remember { SnackbarHostState() }
        val snackScope = rememberCoroutineScope()

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
            Button(onClick = {
                submitAnnouncement(
                    title.value,
                    description.value,
                    url.value,
                    snackState,
                    snackScope,
                    snackBarMessage
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
        snackBarMessage: MutableState<String>
    ) {
        if (title.isNotEmpty() && description.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance().reference.child("announcements")
            val newAnnouncement = hashMapOf(
                "timestamp" to ServerValue.TIMESTAMP,
                "title" to title,
                "description" to description,
                "url" to url
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


    @Preview
    @Composable
    fun AddAnnouncementsScreenPreview() {
        AddAnnouncementsScreen()
    }
}
package com.kartik.tutordashboard.Tutor

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import coil.compose.rememberImagePainter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.kartik.tutordashboard.R
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class UploadAssignment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tutor_upload_assignment, container, false)
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)
        composeView.apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // In Compose world
                MaterialTheme {
                    UploadAssignmentScreen()
                }
            }
        }
        return view

    }


    @Composable
    fun UploadAssignmentScreen() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val snackBarMessage = remember { mutableStateOf("") }
        val snackScope = rememberCoroutineScope()
        val snackState = remember { SnackbarHostState() }
        var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
        var fileType by remember { mutableStateOf<FileType?>(null) }
        val onUploadChangeState = remember { mutableStateOf<Uri?>(null) }
        val title = remember { mutableStateOf("") }
        val description = remember { mutableStateOf("") }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                fileType = FileType.IMAGE
            }
        }

        val pdfPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                fileType = FileType.PDF
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Upload Assignment", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text(text = "Pick Image")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(onClick = { pdfPickerLauncher.launch("application/pdf") }) {
                    Text(text = "Pick PDF")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            selectedFileUri?.let { uri ->
                when (fileType) {
                    FileType.IMAGE -> {
                        Image(
                            painter = rememberImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    FileType.PDF -> {
                        Text(
                            text = "PDF Selected: $uri",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                    else -> { /* No file selected */ }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                    coroutineScope.launch {
                        uploadFile(selectedFileUri,onUploadChangeState,snackBarMessage )
                } ?: run {
                    coroutineScope.launch {
                        snackState.showSnackbar("Please select a file to upload.")
                    }
                }
            }) {
                Text(text = "Upload")
            }

            Spacer(modifier = Modifier.height(20.dp))

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

            SnackbarHost(hostState = snackState)

            Button(onClick = {
                coroutineScope.launch {
                    submitAssignment(title = title.value , description = description.value,
                        selectedUri = onUploadChangeState , snackBarMessage = snackBarMessage )
                } ?: run {
                    coroutineScope.launch {
                        snackState.showSnackbar("Please select a file to upload.")
                    }
                }
            }) {
                Text(text = "Submit")
            }

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



    }

    enum class FileType {
        IMAGE, PDF
    }

    fun uploadFile(fileUri: Uri?, selectedUri: MutableState<Uri?>,snackBarMessage: MutableState<String>) {
        val context = requireContext()

        fileUri?.let {
            val mimeType = context.contentResolver.getType(it)

            val storageReference = FirebaseStorage.getInstance().reference.child("assignments")

            when {
                mimeType?.startsWith("image/") == true -> {
                    // Handle image upload
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, fileUri)

                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                        val data = outputStream.toByteArray()

                        val fileName = getRandomString(10)
                        val photoRef = storageReference.child("$fileName.jpg")
                        val uploadTask = photoRef.putBytes(data)
                        uploadTask.addOnSuccessListener { taskSnapshot ->
                            photoRef.downloadUrl.addOnSuccessListener { uri ->
                                Log.d("uri", uri.toString())
                                selectedUri.value = uri
                            }
                            snackBarMessage.value = "Uploaded Image successfully"

                        }.addOnFailureListener { e ->
                            Log.e("ProfileFragment", "Error uploading image: ${e.message}")
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "Error processing image: ${e.message}")
                    }
                }
                mimeType?.startsWith("application/pdf") == true -> {
                    // Handle PDF upload
                    val fileName = getRandomString(10)
                    val pdfRef = storageReference.child("$fileName.pdf")
                    val uploadTask = pdfRef.putFile(fileUri)
                    uploadTask.addOnSuccessListener { taskSnapshot ->
                        pdfRef.downloadUrl.addOnSuccessListener { uri ->
                            Log.d("uri", uri.toString())
                            selectedUri.value = uri
                        }
                        snackBarMessage.value = "Uploaded PDF successfully"
                    }.addOnFailureListener { e ->
                        Log.e("ProfileFragment", "Error uploading PDF: ${e.message}")
                    }
                }
                else -> {
                    Log.e("ProfileFragment", "Unsupported file type: $mimeType")
                }
            }
        } ?: run {
            Log.e("ProfileFragment", "No file selected")
        }
    }


    fun submitAssignment(
        title: String,
        description: String,
        snackBarMessage: MutableState<String>,
        selectedUri: MutableState<Uri?>
    ) {
        if (title.isNotEmpty() && description.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance().reference.child("assignments")
            val newAnnouncement = hashMapOf(
                "timestamp" to ServerValue.TIMESTAMP,
                "title" to title,
                "description" to description,
                "url" to selectedUri.value.toString(),
            )
            database.push().setValue(newAnnouncement)
                .addOnSuccessListener {
                    // Show the Snackbar on success
                    snackBarMessage.value = "Assignment Created Successfully"
                }
                .addOnFailureListener { exception ->
                    // Handle failure (show error message)
                    Log.w("AddAssignment", "Error adding assignment", exception)
                    snackBarMessage.value = "Error adding assignment"
                }
        } else {
            // Handle empty fields (show error message)
            snackBarMessage.value = "Title and Description cannot be empty"
        }
    }


    private fun getRandomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    @Preview
    @Composable
    fun PreviewUploadAssignments(){
        UploadAssignmentScreen()
    }
}
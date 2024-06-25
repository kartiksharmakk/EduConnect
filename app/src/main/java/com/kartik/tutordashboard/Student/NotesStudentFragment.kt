package com.kartik.tutordashboard.Student

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.R

class NotesStudentFragment: Fragment() {

    val notesList = mutableStateListOf<DataModel.Notes>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes_student, container, false)
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)
        fetchNotes()
        composeView.apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // In Compose world
                MaterialTheme {
                    ShowNotesScreen()
                }
            }
        }
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton = view.findViewById<ImageView>(R.id.backButton)

        (activity as? StudentHome)?.hideBottomNavigation()

    }

    @Composable
    fun ShowNotesScreen(){

        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(top = 32.dp)
        ) {
            items(notesList) { note ->
                NotesCard(
                    title = note.title,
                    description = note.description,
                    url = note.url,
                    url_link = note.url_link
                )
            }
        }
    }

    @Composable
    fun NotesCard(title: String, description: String, url: String, url_link: String) {
        val context = LocalContext.current

        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            elevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                if (url != "null") {
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clickable {
                                val intent = Intent().apply {
                                    action = Intent.ACTION_VIEW
                                    setDataAndType(Uri.parse(url), "image/*")
                                }
                                startActivity(intent)
                            }
                        ,
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url_link))
                                    context.startActivity(intent)

                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.d("NoteClick", "Cannot Handle this type of url")
                                }
                            }
                    )
                }
            }
        }
    }
    private fun fetchNotes() {
        val database = FirebaseDatabase.getInstance().reference.child("notes")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (announcementSnapshot in snapshot.children) {
                    val announcement = announcementSnapshot.getValue(DataModel.Notes::class.java)
                    if (announcement != null) {
                        notesList.add(announcement)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.d("FetchNotes","Unable to fetch notes")
            }
        })
    }

    @Preview
    @Composable
    fun previewShowAnnouncementScreen(){
        ShowNotesScreen()
    }
}
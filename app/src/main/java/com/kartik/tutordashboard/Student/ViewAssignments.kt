package com.kartik.tutordashboard.Student

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.R


class ViewAssignments : Fragment() {

    val assignmentsList = mutableStateListOf<DataModel.Assignment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_view_assignment, container, false)
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)
        fetchAssignments()
        composeView.apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // In Compose world
                MaterialTheme {
                    ShowAssignmentsScreen()
                }
            }
        }
        return view

    }

    @Composable
    fun ShowAssignmentsScreen(){

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
        ) {
            items(assignmentsList) { assignments ->
                AssignmentCard(
                    title = assignments.title,
                    description = assignments.description,
                    url = assignments.url,
                )
            }
        }
    }

    @Composable
    fun AssignmentCard(title: String, description: String, url: String) {
        val context = LocalContext.current
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                ,
            shape = RoundedCornerShape(8.dp),
            elevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {

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
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)

                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.d("AnnouncementClick", "Cannot Handle this type of url")
                                }
                            }
                    )
                }
            }
        }
    }
    private fun fetchAssignments() {
        val database = FirebaseDatabase.getInstance().reference.child("assignments")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (announcementSnapshot in snapshot.children) {
                    val assignment = announcementSnapshot.getValue(DataModel.Assignment::class.java)
                    if (assignment != null) {
                        assignmentsList.add(assignment)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.d("FetchAnnouncements","Unable to fetch announcements")
            }
        })
    }

    @Preview
    @Composable
    fun previewShowAnnouncementScreen(){
        ShowAssignmentsScreen()
    }

}

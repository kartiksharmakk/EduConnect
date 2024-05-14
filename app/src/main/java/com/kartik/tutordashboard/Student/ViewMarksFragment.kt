package com.kartik.tutordashboard.Student

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.databinding.FragmentViewMarksBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class ViewMarksFragment : Fragment() {
    lateinit var binding: FragmentViewMarksBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var uid: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewMarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseDatabase = Firebase.database
        databaseReference = firebaseDatabase.getReference("tests")
        uid = Prefs.getUID(requireContext())!!
        val testId = AttemptTestFragmentArgs.fromBundle(requireArguments()).testId
        databaseReference.child(testId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val test = snapshot.getValue(DataModel.Test::class.java)
                    if(test != null){
                        val totalMarks = test.totalMarks
                        binding.txtTotalMarks.text = totalMarks.toString()
                        binding.txtViewTestName.text = test.testName
                        val assignedTo = test.assignedTo.find { it.studentId == uid }
                        if(assignedTo != null){
                            val marksObtained = assignedTo.marks
                            binding.txtMarksObtained.text = marksObtained.toString()
                            val percentage = (marksObtained*100)/totalMarks
                            binding.txtPercentage.text = "${percentage}%"
                            binding.ratingBar.max = 5
                            binding.ratingBar.stepSize = 0.5f
                            binding.ratingBar.rating = percentage.toFloat()/20
                            getRemarksAndImage(percentage)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
               Log.d("ViewMarksFragment","Failed to fetch test details")
            }

        })
    }

    fun getRemarksAndImage(percentage: Int){
        val drawableId: Int
        binding.apply {
            when (percentage) {
                in 80..100 -> {
                    drawableId = R.drawable.percentage100
                    txtRemarks.text = "Excellent work! You aced this!"
                }
                in 60..79 -> {
                    drawableId = R.drawable.percentage80
                    txtRemarks.text = "Solid performance. Keep it going!"
                }
                in 40..59 -> {
                    drawableId = R.drawable.percentage60
                    txtRemarks.text = "Steady progress! Keep practicing and you'll improve even more."
                }
                in 20..39 -> {
                    drawableId = R.drawable.percentage40
                    txtRemarks.text = "Don't be discouraged. Focus on improvement and you'll get there."
                }
                else -> {
                    drawableId = R.drawable.percentage20
                    txtRemarks.text = "We believe in you! Don't worry, try again and you'll do better."
                }
            }

            imgTestReview.setImageResource(drawableId)  // Set the image resource based on the calculated drawableId
        }

    }
}
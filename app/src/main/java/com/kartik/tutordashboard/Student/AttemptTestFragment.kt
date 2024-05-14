package com.kartik.tutordashboard.Student

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartik.tutordashboard.Adapter.AttemptTestAdapter
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.databinding.FragmentAttemptTestBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class AttemptTestFragment : Fragment() {
    lateinit var binding: FragmentAttemptTestBinding
    lateinit var adapter: AttemptTestAdapter
    val questionsList = mutableListOf<DataModel.Question>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAttemptTestBinding.inflate(inflater, container, false)
        val testId = AttemptTestFragmentArgs.fromBundle(requireArguments()).testId
        adapter = AttemptTestAdapter(requireContext(), questionsList)
        binding.apply {
            imgBackAttemptTest.setOnClickListener{
                findNavController().navigate(R.id.tutorHomeFragment)
            }
            rvAttemptTest.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = this@AttemptTestFragment.adapter
            }
            binding.imgSubmitTest.setOnClickListener {
                val uid = Prefs.getUID(requireContext())!!
                val attemptedTest = createAttemptTest()
                saveAttemptedTestToDatabase(uid, attemptedTest)
                updateStatusAndMarks(uid,attemptedTest)
            }
        }
        fetchQuestions(testId)
        return binding.root
    }

    private fun fetchQuestions(testId: String) {
        val testsReference = Firebase.database.getReference("tests").child(testId)
        testsReference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val test = snapshot.getValue(DataModel.Test::class.java)
                    questionsList.clear()
                    test?.let{
                        binding.txtTestNameSt.text = it.testName
                        val questions = it.questions
                        questionsList.addAll(questions)
                    }
                    adapter.notifyDataSetChanged()
                }else{
                    Log.e("AttemptTestFragment", "Test with ID $testId does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AttemptTestFragment", "Error fetching test data: ${error.message}")
            }

        })
    }

    private fun createAttemptTest(): DataModel.Test{
        val testId = AttemptTestFragmentArgs.fromBundle(requireArguments()).testId
        val testName = binding.txtTestNameSt.text.toString()
        val attemptedQuestions = mutableListOf<DataModel.Question>()
        for(question in questionsList){
            val attemptedQuestion = DataModel.Question(question.questionId,
                question.text,
                question.options,
                question.correctOption,
                question.marks,
                question.selectedOption)
            attemptedQuestions.add(attemptedQuestion)
        }
        return DataModel.Test(
            testId,
            testName,
            questions = attemptedQuestions
        )
    }
    private fun saveAttemptedTestToDatabase(studentId: String, test: DataModel.Test){
        val database = Firebase.database
        val studentRef = database.getReference("Student")
        studentRef.orderByChild("studentId").equalTo(studentId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(studentSnapshot in snapshot.children){
                            val studentKey = studentSnapshot.key
                            studentKey?.let{key->
                                val attemptedTestsRef = studentRef.child(key).child("attemptedTests").push()
                                attemptedTestsRef.setValue(test).addOnSuccessListener {
                                }.addOnFailureListener { exception->
                                    Log.e("AttemptTestFragment", "Error saving attempted test: ${exception.message}")
                                }
                            }
                        }
                    }else{
                        Log.e("AttemptTestFragment", "No student found with UID: $studentId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AttemptTestFragment", "Error querying student data: ${error.message}")
                }
            })
    }

    private fun updateStatusAndMarks(studentId: String,test: DataModel.Test){
        val database = Firebase.database
        val testId = test.testId
        val testRef = database.getReference("tests").child(testId)
        testRef.child("assignedTo").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for(assignedToSnapshot in snapshot.children){
                        val assignedToStudentId = assignedToSnapshot.child("studentId").getValue()
                        if(assignedToStudentId == studentId){
                            assignedToSnapshot.ref.updateChildren(
                                mapOf(
                                    "hasAttempted" to true,
                                    "marks" to calculateTotalMarks(test)
                                )
                            ).addOnSuccessListener {
                                Log.d("AttemptTestFragment", "Test status and marks updated successfully for student $studentId")
                            }.addOnFailureListener { error ->
                                Log.e("AttemptTestFragment", "Error updating test status and marks for student $studentId: ${error.message}")
                            }
                            return
                        }
                    }
                }else{
                    Log.e("AttemptTestFragment", "No assignedTo data found for test $testId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AttemptTestFragment", "Error querying assignedTo data: ${error.message}")
            }

        })
    }

    private fun calculateTotalMarks(test: DataModel.Test): Int {
        // Iterate through the attempted questions and sum up the marks
        var totalMarks = 0
        for (question in test.questions) {
            if (question.selectedOption == question.correctOption) {
                totalMarks += question.marks
            }
        }
        return totalMarks
    }

}
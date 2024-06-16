package com.kartik.tutordashboard.Data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Random

class TestRepository(val database: FirebaseDatabase) {
    private val testRef = database.getReference("tests")
    private val groupRef = database.getReference("Groups")

    fun generateTestId(): String{
        return "test${testRef.push().key!!}"
    }
    fun generateQuestionId(testId: String): String{
        return  testRef.child(testId).child("questions").push().key!!
    }

    fun generateRandomId(length: Int): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') // Define the character pool
        val random = Random()

        return (1..length)
            .map { random.nextInt(charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    fun saveTestAndQuestions(testId: String, testName: String,questions: ArrayList<DataModel.Question>,uid:String?,studentsList: ArrayList<String>, totalMarks: Int){
        //testRef.child(testId).setValue(DataModel.Test(testId,"", emptyList(), emptyList()))//Add data

        for( i in questions){
            i.questionId = generateRandomId(8)
        }

        val testAssignedToStudentsList = ArrayList<DataModel.TestAssignedTo>()

        for (studentId in studentsList) {
            val testAssignedTo = DataModel.TestAssignedTo(studentId, false)
            testAssignedToStudentsList.add(testAssignedTo)
        }


        testRef.child(testId).setValue(uid?.let {
            DataModel.Test(testId,testName,
                it, testAssignedToStudentsList , questions, totalMarks)
        })

        /*
        questions.forEach { question ->
            val questionId = generateQuestionId(testId)
            question.questionId = questionId
            saveQuestion(question, testId)
        }

         */
        Log.d("TestRepository","Test saved to DB")
    }

    private fun saveQuestion(question: DataModel.Question, testId: String){
        val questionRef = testRef.child(testId).child("questions")
        questionRef.child(question.questionId).setValue(question)
        Log.d("TestRepository","Question saved to DB")
    }
    fun addQuestionToTest(testId: String, question: DataModel.Question){
        val questionId = testRef.child("$testId/questions").push().key
        question.questionId = questionId!!
        testRef.child("$testId/questions").child(questionId!!).setValue(question)
    }
    fun getTestByStudentId(studentId: String): LiveData<List<DataModel.Test>>{
        val liveDataTests = MutableLiveData<List<DataModel.Test>>()
        val valueEventListener = testRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val testList = mutableListOf<DataModel.Test>()
                    for(child in snapshot.children){
                        val test = child.getValue(DataModel.Test::class.java)
                      /*  if(test != null && test.assignedTo.contains(studentId)){
                            testList.add(test)
                        }*/
                    }
                    liveDataTests.postValue(testList.toList())
                }else{
                    liveDataTests.postValue(emptyList())
                }
            }
            override fun onCancelled(error: DatabaseError) {
               
            }

        })

        return liveDataTests
    }

    fun getTestByTutorId(tutorId: String): LiveData<List<DataModel.Test>>{
        val liveDataTests = MutableLiveData<List<DataModel.Test>>()
        val valueEventListener = testRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val testList = mutableListOf<DataModel.Test>()
                    for (child in snapshot.children) {
                        val test = child.getValue(DataModel.Test::class.java)
                        if (test != null && test.creatorId == tutorId) {
                            testList.add(test)
                        }
                    }
                    liveDataTests.postValue(testList.toList())
                } else {
                    liveDataTests.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
               
                liveDataTests.postValue(emptyList())
            }

        })

        return liveDataTests
    }

    fun getTestByTestId(testId: String): LiveData<DataModel.Test?>{
        val testRef = testRef.child(testId)
        val liveDataTest = MutableLiveData<DataModel.Test?>()
        val valueEventListener = testRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val test = snapshot.getValue(DataModel.Test::class.java)
                    liveDataTest.postValue(test)
                }else{
                    liveDataTest.postValue(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
               
                liveDataTest.postValue(null)
            }
        })
        return  liveDataTest
    }

    fun getAllTests(): LiveData<List<DataModel.Test>>{
        val liveDataTests = MutableLiveData<List<DataModel.Test>>()
        val valueEventListener = testRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val testList = mutableListOf<DataModel.Test>()
                    for(child in snapshot.children){
                        val test = child.getValue(DataModel.Test::class.java)
                        if(test != null){
                            testList.add(test)
                        }
                    }
                    liveDataTests.postValue(testList.toList())
                }else{
                    liveDataTests.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
               
                liveDataTests.postValue(emptyList())
            }
        })
        return  liveDataTests
    }

    fun getStudentByGroup(groupId: String): List<String>{
        val students: MutableList<String> = mutableListOf()
        val query = groupRef.orderByChild("groupId").equalTo(groupId)
        query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (groupSnapshot in snapshot.children){
                    for(studentSnapshot in groupSnapshot.child("students").children){
                        val studentId = studentSnapshot.value.toString()
                        studentId?.let { students.add(it) }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
               
            }

        })
        return students
    }
}
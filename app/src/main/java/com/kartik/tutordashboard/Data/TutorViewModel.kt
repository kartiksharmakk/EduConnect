package com.kartik.tutordashboard.Data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.launch

class TutorViewModel: ViewModel() {

    private val mutuableAbout = MutableLiveData<String>()
    private val _selectedStudentsCount = MutableLiveData<Int>()
    private val _selectedStudentsIds = MutableLiveData<Set<String>>()


    val firebaseDatabase = Firebase.database
    val databaseReference = firebaseDatabase.getReference("User Details")
    val about: LiveData<String> get() = mutuableAbout
    val selectedStudentsCount: LiveData<Int> get() = _selectedStudentsCount
    val selectedStudentIds: LiveData<Set<String>> get() = _selectedStudentsIds
    fun updateAbout(context: Context,text: String){
        val email = Prefs.getUSerEmailEncoded(context)
        viewModelScope.launch {
            mutuableAbout.value = text
        }
        databaseReference.child(email!!).child("about").setValue(text)
    }

    fun addSelectedStudent(studentId: String){
        val currentSelectedStudentIds = _selectedStudentsIds.value ?.toMutableSet() ?: mutableSetOf()
        currentSelectedStudentIds?.add(studentId)
        _selectedStudentsIds.value = currentSelectedStudentIds
        _selectedStudentsCount.value = currentSelectedStudentIds?.size
        Log.d("TutorViewModel","AddSelectedStudent: \nStudentIDs : ${_selectedStudentsIds.value} \nCount : ${_selectedStudentsCount.value}")
    }

    fun removeSelectedStudent(studentId: String){
        val currentSelectedIds = _selectedStudentsIds.value ?: emptySet()
        val updateSelectedIds = currentSelectedIds.toMutableSet().apply { remove(studentId) }
        _selectedStudentsIds.value = updateSelectedIds
        _selectedStudentsCount.value = updateSelectedIds.size
        Log.d("TutorViewModel","removeSelectedStudents: \nCount: ${_selectedStudentsCount.value}")
    }

}
package com.kartik.tutordashboard.Student

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.kartik.tutordashboard.Adapter.StudentGroupAdapter
import com.kartik.tutordashboard.Adapter.StudentTestAdapter
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.databinding.FragmentStudentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class StudentHomeFragment : Fragment() {
    lateinit var binding: FragmentStudentHomeBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var groupReference: DatabaseReference
    lateinit var testReference: DatabaseReference
    lateinit var groupAdapter: StudentGroupAdapter
    lateinit var testAdapter: StudentTestAdapter
    lateinit var groupList: MutableList<DataModel.Group>
    lateinit var testList: MutableList<DataModel.Test>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentHomeBinding.inflate(inflater, container, false)
        firebaseDatabase = Firebase.database
        groupReference = firebaseDatabase.getReference("Groups")
        testReference = firebaseDatabase.getReference("tests")
        val name = Prefs.getUsername(requireContext())
        groupList = mutableListOf()
        testList = mutableListOf()
        groupAdapter = StudentGroupAdapter(requireContext(), groupList){group ->
            val action = StudentHomeFragmentDirections.actionStudentHomeFragmentToGroupDetailsFragment(group.groupId)
            findNavController().navigate(action)
        }
        testAdapter = StudentTestAdapter(requireContext(), testList){test ->
            val testId = test.testId
            val status = getStatusForTestId(testId)
            val action = if(status){
                StudentHomeFragmentDirections.actionStudentHomeFragmentToViewMarksFragment(test.testId)
            }else{
                StudentHomeFragmentDirections.actionstudentHomeFragmentToAttemptTestFragment(test.testId)
            }
            findNavController().navigate(action)
        }

        binding.apply {
            txtUsernameStudentHome.setText(name)
            rvGroupsStudent.adapter = groupAdapter
            rvTestsStudent.adapter = testAdapter
        }

        fetchGroups()
        fetchTests()
        return binding.root
    }

    private fun fetchGroups(){
        val uid = Prefs.getUID(requireContext())
        groupReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                groupList.clear()
                for (groupSnapshot in snapshot.children) {
                    val group = groupSnapshot.getValue(DataModel.Group::class.java)
                    group?.let {
                        if (it.students.contains(uid)) {
                            groupList.add(it)
                        }
                    }
                }
                groupAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("StudentHomeFragment", "$error")
            }
        })
    }

    private fun fetchTests(){
        val uid = Prefs.getUID(requireContext())
        testReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                testList.clear()
                var completed = 0
                for (testSnapshot in snapshot.children) {
                    val test = testSnapshot.getValue(DataModel.Test::class.java)
                    test?.let {
                        if (it.assignedTo.any { assignedTo -> assignedTo.studentId == uid }) {
                            if(it.assignedTo.any { x -> x.hasAttempted == true }){
                                completed++
                            }
                            testList.add(it)
                        }
                    }
                }
                Prefs.saveAttemptedTestCount(requireContext(), completed)
                Prefs.saveAllotedTestCount(requireContext(),testList.size)
                testAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("StudentHomeFragment", "$error")
            }
        })
    }

    private fun getStatusForTestId(testId: String): Boolean{
        val studentId = Prefs.getUID(requireContext())
        val test = testList.find { it.testId == testId }
        if(test != null){
            val assignedTo = test.assignedTo.find { it.studentId == studentId }
            if(assignedTo != null){
                return if(assignedTo.hasAttempted){
                    true
                }else{
                    false
                }
            }
        }
        return false
    }
}
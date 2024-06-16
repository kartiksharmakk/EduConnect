package com.kartik.tutordashboard.Student

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.kartik.tutordashboard.Adapter.AdapterAttemptedTests
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.databinding.FragmentAttemptedTestsBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class AttemptedTestsFragment : Fragment() {
    lateinit var binding: FragmentAttemptedTestsBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var testReference: DatabaseReference
    lateinit var pendingTests: MutableList<DataModel.Test>
    lateinit var attemptedAdapter: AdapterAttemptedTests
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAttemptedTestsBinding.inflate(inflater, container, false)
        firebaseDatabase = Firebase.database
        testReference = firebaseDatabase.getReference("tests")
        pendingTests = mutableListOf()
        attemptedAdapter = AdapterAttemptedTests(requireContext(), pendingTests){tests->
            val action = AttemptTestFragmentDirections.actionAttemptedTestsFragmentToAttemptedTestFragment(tests.testId)
            findNavController().navigate(action)
        }
        binding.rvAttemptedTests.adapter = attemptedAdapter
        retrieveTests()
        return binding.root
    }

    fun retrieveTests(){
        val uid = Prefs.getUID(requireContext())
        testReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(testSnapshot in snapshot.children){
                    val test = testSnapshot.getValue(DataModel.Test::class.java)
                    test?.let {
                        val assignedTo = it.assignedTo.find { assignedTo -> assignedTo.studentId == uid }
                        if(assignedTo != null){
                            if(assignedTo.hasAttempted){
                                pendingTests.add(it)
                            }
                        }
                    }
                }
                attemptedAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
               
            }

        })

    }
}
package com.kartik.tutordashboard.Tutor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartik.tutordashboard.Adapter.SelectStudentsAdapter
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.TutorViewModel
import com.kartik.tutordashboard.databinding.FragmentStudentListBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class StudentListFragment : Fragment() {
    lateinit var binding: FragmentStudentListBinding
    lateinit var auth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: SelectStudentsAdapter
    val viewModel: TutorViewModel by activityViewModels<TutorViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentListBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = Firebase.database
        databaseReference = firebaseDatabase.getReference("Student")
        showRecyclerView()
        loadStudents()
        binding.imgBackAddStudents.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.imgSubmit.isEnabled = false
        binding.imgSubmit.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Observer()
    }
    fun showRecyclerView(){
        adapter = SelectStudentsAdapter(requireContext(), ArrayList(), viewModel)
        binding.recyclerAddStudent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@StudentListFragment.adapter
        }
    }

    private fun loadStudents() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val students = ArrayList<DataModel.Students>()
                for (studentSnapshot in snapshot.children) {
                    val student = studentSnapshot.getValue(DataModel.Students::class.java)
                    student?.let {
                        students.add(student)
                    }
                }
                adapter.list = students
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    fun Observer(){
        viewModel.selectedStudentsCount.observe(viewLifecycleOwner){
            if (it != null){
                if(it == 0){
                    binding.imgSubmit.isEnabled = false
                }else{
                    binding.imgSubmit.isEnabled = true
                }
            }
        }
    }
}
package com.kartik.tutordashboard.Tutor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartik.tutordashboard.Adapter.AdapterStudentTest
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.TestRepository
import com.kartik.tutordashboard.Data.TestViewModel
import com.kartik.tutordashboard.Data.TestViewModelFactory
import com.kartik.tutordashboard.databinding.FragmentAllotTestToStudentsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import org.greenrobot.eventbus.EventBus

class AllotTestToStudentsFragment : Fragment() , AdapterStudentTest.onClickStudentListener{
    lateinit var binding: FragmentAllotTestToStudentsBinding
    lateinit var auth: FirebaseAuth
    var firebaseDatabase = Firebase.database
    lateinit var databaseReference: DatabaseReference
    lateinit var adapter: AdapterStudentTest
    val testRepository = TestRepository(firebaseDatabase)
    val viewModelFactory = TestViewModelFactory(testRepository)
    val viewModel: TestViewModel by viewModels { viewModelFactory }
    var studentsList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = Firebase.database
        databaseReference = firebaseDatabase.getReference("Student")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllotTestToStudentsBinding.inflate(inflater, container, false)
        showRecyclerView()
        loadStudents()

        return binding.root
    }

    fun showRecyclerView(){
        adapter = AdapterStudentTest(requireContext(), ArrayList(), viewModel ,this)
        binding.rvAllotToStudents.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AllotTestToStudentsFragment.adapter
        }
    }

    fun loadStudents(){
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val students = ArrayList<DataModel.Students>()
                for(studentSnapshot in snapshot.children){
                    val student = studentSnapshot.getValue(DataModel.Students::class.java)
                    student?.let {
                        students.add(student)
                    }
                    adapter.list = students
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
               
            }

        })
    }

    override fun onPassArray(passedArray: ArrayList<String>) {
        EventBus.getDefault().post(passedArray)
        //viewModel.saveStudentsIdsList(mostRecentStudentidList)
    }

}
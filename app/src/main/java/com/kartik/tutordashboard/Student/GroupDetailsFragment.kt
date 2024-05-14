package com.kartik.tutordashboard.Student

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.kartik.tutordashboard.Adapter.GroupMembersAdapter
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.databinding.FragmentGroupDetailsBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class GroupDetailsFragment : Fragment() {
    lateinit var binding: FragmentGroupDetailsBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var groupReference: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val groupId = GroupDetailsFragmentArgs.fromBundle(requireArguments()).groupId
        firebaseDatabase = Firebase.database
        groupReference = firebaseDatabase.getReference("Groups").child(groupId)
        groupReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val group = snapshot.getValue(DataModel.Group::class.java)!!
                    binding.apply {
                        imgBackGroupDetails.setOnClickListener{
                            findNavController().popBackStack()
                        }
                        txtGroupNameView.text = group.groupName
                        txtGroupDescription.text = group.description
                        getTutorName(group.tutorId)
                        Glide.with(requireContext()).load(group.displayImage).into(imgGroupDPView)
                        Glide.with(requireContext()).load(group.coverImage).into(imgGroupCoverImageView)
                        fetchStudents(group.students)
                    }
                }else{

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getTutorName(tutorId: String){
        firebaseDatabase = Firebase.database
        val tutorReference = firebaseDatabase.getReference("Tutor")
        tutorReference.orderByChild("uid").equalTo(tutorId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(tutorSnapshot: DataSnapshot) {
                    if (tutorSnapshot.exists()) {
                        for (tutorData in tutorSnapshot.children) {
                            val tutor = tutorData.getValue(DataModel.TeacherModel::class.java)
                            binding.txtGroupCreator.text = tutor?.name!!
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

    }

    fun fetchStudents(studentIds: List<String>){
        firebaseDatabase = Firebase.database
        val studentRefernce = firebaseDatabase.getReference("Student")
        val studentList: MutableList<DataModel.Students> = mutableListOf()
        studentIds.forEach { studentId->
            studentRefernce.orderByChild("studentId").equalTo(studentId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(studentData in snapshot.children){
                                val student = studentData.getValue(DataModel.Students::class.java)
                                student?.let {
                                    studentList.add(it)
                                    if (studentList.size == studentIds.size) {
                                        setupRecyclerView(studentList)
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }

    private fun setupRecyclerView(studentList: List<DataModel.Students>) {
        val adapter = GroupMembersAdapter(studentList)
        binding.rvMembers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMembers.adapter = adapter
    }
}
package com.kartik.tutordashboard.Tutor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartik.tutordashboard.Adapter.AdapterGroupTest
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.TestRepository
import com.kartik.tutordashboard.Data.TestViewModel
import com.kartik.tutordashboard.Data.TestViewModelFactory
import com.kartik.tutordashboard.databinding.FragmentAllotTestToGroupBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import org.greenrobot.eventbus.EventBus

class AllotTestToGroupFragment : Fragment() , AdapterGroupTest.OnClickGroupClickListener{
    lateinit var binding: FragmentAllotTestToGroupBinding
    lateinit var auth: FirebaseAuth
    var firebaseDatabase = Firebase.database
    lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: AdapterGroupTest
    val testRepository = TestRepository(firebaseDatabase)
    val viewModelFactory = TestViewModelFactory(testRepository)
    val groups = ArrayList<DataModel.Group>()
    val viewModel: TestViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        databaseReference = firebaseDatabase.getReference("Groups")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllotTestToGroupBinding.inflate(inflater, container, false)
        showRecyclerView()
        loadStudents()

        return binding.root
    }

    fun showRecyclerView(){
        adapter = AdapterGroupTest(requireContext(), ArrayList(), viewModel,this)
        binding.rvAllotToGroup.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AllotTestToGroupFragment.adapter
        }
    }

    fun loadStudents(){
        databaseReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(groupSnapshot in snapshot.children){
                    val group = groupSnapshot.getValue(DataModel.Group::class.java)
                    group?.let {
                        groups.add(group)
                    }
                }
                adapter.list = groups
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
               
            }

        })
    }


    override fun onPassArray(passedArray: ArrayList<String>) {
        val selectedStudentIds = ArrayList<String>()

        for (groupId in passedArray) {
            val matchingGroup = groups.find { it.groupId == groupId }

            matchingGroup?.let { group ->
                selectedStudentIds.addAll(group.students)
            }
        }

        val uniqueStudentIds = ArrayList(selectedStudentIds.toSet().toList())

        EventBus.getDefault().post(uniqueStudentIds)

    }

}
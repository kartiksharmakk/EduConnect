package com.kartik.tutordashboard.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.databinding.CustomTestLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class AdapterAttemptedTests(val context: Context, val tests: List<DataModel.Test>, val onItemClick: (DataModel.Test) -> Unit): RecyclerView.Adapter<AdapterAttemptedTests.TestViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterAttemptedTests.TestViewHolder {
        val binding = CustomTestLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdapterAttemptedTests.TestViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AdapterAttemptedTests.TestViewHolder, position: Int) {
        val test = tests[position]
        holder.bind(context, test)
    }

    override fun getItemCount(): Int {
        return tests.size
    }

    class TestViewHolder(val binding: CustomTestLayoutBinding, val onItemClick: (DataModel.Test) -> Unit): RecyclerView.ViewHolder(binding.root), View.OnClickListener{
        fun bind(context: Context, test: DataModel.Test){
            binding.apply {
                tutorName(test.creatorId)
                txtTestName1.text = test.testName
                root.setOnClickListener {
                    onItemClick.invoke(test)
                }
            }
        }

        fun tutorName(uid: String){
            val tutorReference = Firebase.database.getReference("Tutor")
            tutorReference.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for(tutorSnapshot in snapshot.children){
                            val tutorName = tutorSnapshot.child("name").getValue().toString()
                            binding.txtCreatorName.text = tutorName
                            break
                        }
                    } else {
                        Log.e("AdapterPendingTest", "No tutor found for UID: $uid")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
        override fun onClick(p0: View?) {

        }

    }
}
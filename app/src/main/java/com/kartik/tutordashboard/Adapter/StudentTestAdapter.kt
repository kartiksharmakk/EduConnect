package com.kartik.tutordashboard.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.databinding.CustomTestCardBinding

class StudentTestAdapter(val context: Context, val tests: List<DataModel.Test>, val onItemClick: (DataModel.Test) -> Unit): RecyclerView.Adapter<StudentTestAdapter.TestViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StudentTestAdapter.TestViewHolder {
        val binding = CustomTestCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentTestAdapter.TestViewHolder(binding, onItemClick)
    }

    override fun getItemCount(): Int {
        return tests.size
    }

    override fun onBindViewHolder(holder: StudentTestAdapter.TestViewHolder, position: Int) {
        val test = tests[position]
        holder.bind(context, test)
    }

    class TestViewHolder(val binding: CustomTestCardBinding, val onItemClick: (DataModel.Test) -> Unit): RecyclerView.ViewHolder(binding.root), View.OnClickListener{
        fun bind(context: Context, test: DataModel.Test){
            binding.apply {
                txtTestName.text = test.testName
                root.setOnClickListener {
                    onItemClick.invoke(test)
                }
            }
        }

        override fun onClick(p0: View?) {
            TODO("Not yet implemented")
        }

    }

}
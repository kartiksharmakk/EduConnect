package com.kartik.tutordashboard.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.databinding.CustomTestCardBinding

class TutorTestAdapter(val context: Context, val tests: List<DataModel.Test>, val onItemClick: (DataModel.Test) -> Unit): RecyclerView.Adapter<TutorTestAdapter.GroupViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TutorTestAdapter.GroupViewHolder {
        val binding = CustomTestCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  TutorTestAdapter.GroupViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TutorTestAdapter.GroupViewHolder, position: Int) {
        val test = tests[position]
        holder.bind(context, test)
    }

    override fun getItemCount(): Int {
        return tests.size
    }

    class GroupViewHolder(val binding: CustomTestCardBinding, val onItemClick: (DataModel.Test) -> Unit): RecyclerView.ViewHolder(binding.root), View.OnClickListener{

        fun bind(context: Context, test: DataModel.Test){
            binding.apply {
                txtTestName.text = test.testName
                root.setOnClickListener {
                    onItemClick.invoke(test)
                }
            }
        }
        override fun onClick(p0: View?) {
           
        }

    }
}
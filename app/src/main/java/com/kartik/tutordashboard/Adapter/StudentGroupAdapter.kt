package com.kartik.tutordashboard.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.databinding.CustomGroupCardBinding

class StudentGroupAdapter(val context: Context, val groups: List<DataModel.Group>, val onItemClick: (DataModel.Group) -> Unit): RecyclerView.Adapter<StudentGroupAdapter.GroupViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StudentGroupAdapter.GroupViewHolder {
        val binding = CustomGroupCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentGroupAdapter.GroupViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: StudentGroupAdapter.GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(context, group)
    }

    override fun getItemCount(): Int {
        return  groups.size
    }

    class GroupViewHolder(val binding: CustomGroupCardBinding, val onItemClick: (DataModel.Group) -> Unit): RecyclerView.ViewHolder(binding.root), View.OnClickListener{
        init {
            binding.root.setOnClickListener(this)
        }
        fun bind(context: Context, group: DataModel.Group){
            binding.apply {
                Glide.with(context).load(group.displayImage).into(imgCustom)
                txtCustom.text = group.groupName
                root.setOnClickListener {
                    onItemClick.invoke(group)
                }
            }
        }

        override fun onClick(p0: View?) {

        }
    }
}
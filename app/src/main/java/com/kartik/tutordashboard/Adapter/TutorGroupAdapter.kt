package com.kartik.tutordashboard.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.databinding.CustomGroupCardBinding
class TutorGroupAdapter(val context: Context, val groups: List<DataModel.Group>, val onItemClick: (DataModel.Group) -> Unit): RecyclerView.Adapter<TutorGroupAdapter.GroupViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TutorGroupAdapter.GroupViewHolder {
        val binding = CustomGroupCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TutorGroupAdapter.GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(context,group)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    class GroupViewHolder(val binding: CustomGroupCardBinding,val onItemClick: (DataModel.Group) -> Unit): RecyclerView.ViewHolder(binding.root), View.OnClickListener{
        init{
            binding.root.setOnClickListener(this)
        }

        fun bind(context: Context,group: DataModel.Group){
            Glide.with(context).load(group.displayImage).into(binding.imgCustom)
            binding.apply {
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
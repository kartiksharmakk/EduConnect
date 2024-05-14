package com.kartik.tutordashboard.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.R

class GroupMembersAdapter(private val membersList: List<DataModel.Students>): RecyclerView.Adapter<GroupMembersAdapter.MemberViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupMembersAdapter.MemberViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.custom_select_student, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupMembersAdapter.MemberViewHolder, position: Int) {
        val member = membersList[position]
        holder.bind(member)
    }

    override fun getItemCount(): Int {
        return membersList.size
    }

    class MemberViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val txtMemberName: TextView = itemView.findViewById(R.id.txtSelectStudentName)
        val txtMemberPhone: TextView = itemView.findViewById(R.id.txtSelectStudentPhone)
        val img: ImageView = itemView.findViewById(R.id.imgSelectStudent)
        val cardView: CardView = itemView.findViewById(R.id.cardRadioButton)

        fun bind(member: DataModel.Students){
            txtMemberName.text = member.name
            txtMemberPhone.text = member.phone
            Glide.with(itemView.context).load(member.image).into(img)
            cardView.visibility = View.GONE
        }
    }
}
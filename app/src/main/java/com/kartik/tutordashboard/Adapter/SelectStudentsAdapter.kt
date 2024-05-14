package com.kartik.tutordashboard.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.TutorViewModel
import com.kartik.tutordashboard.databinding.CustomSelectStudentBinding
import com.facebook.shimmer.Shimmer
import com.google.firebase.storage.FirebaseStorage

class SelectStudentsAdapter(var context: Context,var list: List<DataModel.Students>, val viewModel: TutorViewModel): RecyclerView.Adapter<SelectStudentsAdapter.StudentViewHolder>() {

    val selectedStudentIds = mutableListOf<String>()
    class StudentViewHolder(val binding: CustomSelectStudentBinding, private val adapter: SelectStudentsAdapter): RecyclerView.ViewHolder(binding.root){
        fun bind(context: Context, student: DataModel.Students, selectedStudentIds: MutableList<String>, viewModel: TutorViewModel){
            binding.apply {
                val shimmer = Shimmer.AlphaHighlightBuilder()
                    .setDuration(1000) // Adjust shimmer animation duration as needed
                    .setBaseAlpha(0.7f)
                    .setHighlightAlpha(0.6f)
                    .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                    .setAutoStart(true)
                    .build()
                Log.d("Select Student","Image: ${student.image}")
                shimmerSelectStudentName.startShimmer()
                shimmerSelectStudentPhone.startShimmer()

                val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(student.image)
                shimmerSelectStudentImage.setShimmer(shimmer)
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(context)
                        .load(uri)
                        .into(binding.imgSelectStudent)
                }.addOnFailureListener { exception ->
                    Log.e("Select Student", "Failed to get download URL: ${exception.message}")
                }

                txtSelectStudentName.setText(student.name)
                txtSelectStudentPhone.setText(student.phone)

                var isSelected = selectedStudentIds.contains(student.studentId)
                imgRadioButton.visibility = if (isSelected) View.VISIBLE else View.GONE


                cardRadioButton.setOnClickListener {
                    if(isSelected){
                        selectedStudentIds.remove(student.studentId)
                        imgRadioButton.visibility = View.GONE
                        viewModel.removeSelectedStudent(student.studentId)
                    }else{
                        selectedStudentIds.add(student.studentId)
                        imgRadioButton.visibility = View.VISIBLE
                        viewModel.addSelectedStudent(student.studentId)
                    }
                    adapter.notifyItemChanged(adapterPosition)
                    isSelected = !isSelected
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = CustomSelectStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding, this)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = list[position]
        holder.bind(context,student, selectedStudentIds, viewModel)
    }

    fun getSelectStudentIds(): List<String>{
        return selectedStudentIds
    }

}
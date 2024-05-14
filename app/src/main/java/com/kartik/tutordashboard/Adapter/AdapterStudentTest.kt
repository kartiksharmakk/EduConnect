package com.kartik.tutordashboard.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.TestViewModel
import com.kartik.tutordashboard.databinding.CustomSelectStudentBinding
import com.facebook.shimmer.Shimmer
import com.google.firebase.storage.FirebaseStorage

class AdapterStudentTest(var context: Context, var list: List<DataModel.Students>, val viewModel: TestViewModel, onClickStudentListener :AdapterStudentTest.onClickStudentListener ): RecyclerView.Adapter<AdapterStudentTest.TestViewHolder>() {
    val selectedStudentIds = ArrayList<String>()
    val clickStudentListener = onClickStudentListener
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterStudentTest.TestViewHolder {
        val binding = CustomSelectStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: AdapterStudentTest.TestViewHolder, position: Int) {
        val student = list[position]
        holder.bind(context, student, selectedStudentIds, viewModel, clickStudentListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class TestViewHolder(val binding: CustomSelectStudentBinding, val adapter: AdapterStudentTest): RecyclerView.ViewHolder(binding.root){
        fun bind(
            context: Context,
            student: DataModel.Students,
            selectedStudentIds: ArrayList<String>,
            viewModel: TestViewModel,
            clickStudentListener: AdapterStudentTest.onClickStudentListener
        ){
            binding.apply {
                val shimmer = Shimmer.AlphaHighlightBuilder().setDuration(1000).setBaseAlpha(0.7f)
                    .setHighlightAlpha(0.6f).setDirection(Shimmer.Direction.LEFT_TO_RIGHT).setAutoStart(true).build()
                Log.d("AdapterStudentTest","Image: ${student.image}")
                shimmerSelectStudentName.startShimmer()
                shimmerSelectStudentPhone.startShimmer()

                val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(student.image)
                shimmerSelectStudentImage.setShimmer(shimmer)
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(context)
                        .load(uri)
                        .into(imgSelectStudent)
                }.addOnFailureListener { exception ->
                    Log.e("Select Student", "Failed to get download URL: ${exception.message}")
                }

                txtSelectStudentName.setText(student.name)
                txtSelectStudentPhone.setText(student.phone)

                var isSelected = selectedStudentIds.contains(student.studentId)
                imgRadioButton.visibility = if (isSelected) View.VISIBLE else View.GONE

                cardRadioButton.setOnClickListener {
                    val studentId = student.studentId
                    val st = DataModel.TestAssignedTo(studentId, false)
                    if(isSelected){
                        selectedStudentIds.remove(student.studentId)
                        imgRadioButton.visibility = View.GONE
                        viewModel.removeSelectedStudent(st)
                    }else{
                        selectedStudentIds.add(student.studentId)
                        imgRadioButton.visibility = View.VISIBLE
                        //viewModel.addSelectedStudent(st)
                    }
                    clickStudentListener.onPassArray(selectedStudentIds)
                    adapter.notifyItemChanged(adapterPosition)
                    isSelected = !isSelected
                }
            }

        }
    }
    interface onClickStudentListener{
        fun onPassArray(passedArray: ArrayList<String>)
    }
}
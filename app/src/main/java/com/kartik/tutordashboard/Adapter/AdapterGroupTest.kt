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
import com.kartik.tutordashboard.databinding.CustomSelectGroupLayoutBinding
import com.facebook.shimmer.Shimmer
import com.google.firebase.storage.FirebaseStorage

class AdapterGroupTest(var context: Context, var list: List<DataModel.Group>, var viewModel: TestViewModel, onClickGroupListener: AdapterGroupTest.OnClickGroupClickListener): RecyclerView.Adapter<AdapterGroupTest.TestViewHolder>() {
    val selectedGroupIds = ArrayList<String>()
    val clickGroupListener = onClickGroupListener
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterGroupTest.TestViewHolder {
        val binding = CustomSelectGroupLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  TestViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: AdapterGroupTest.TestViewHolder, position: Int) {
        val group = list[position]
        holder.bind(context, group, selectedGroupIds, viewModel,clickGroupListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class TestViewHolder(val binding: CustomSelectGroupLayoutBinding, val adapter: AdapterGroupTest): RecyclerView.ViewHolder(binding.root){
        fun bind(context: Context, group: DataModel.Group, selectedGroupIds: ArrayList<String>, viewModel: TestViewModel, clickGroupListener: AdapterGroupTest.OnClickGroupClickListener){
            binding.apply {
                val shimmer = Shimmer.AlphaHighlightBuilder().setDuration(1000).setBaseAlpha(0.7f)
                    .setHighlightAlpha(0.6f).setDirection(Shimmer.Direction.LEFT_TO_RIGHT).setAutoStart(true).build()
                Log.d("AdapterGroupTest","Image: ${group.displayImage}")
                shimmerSelectStudentName.startShimmer()

                val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(group.displayImage)
                shimmerGroupImg.setShimmer(shimmer)
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(context)
                        .load(uri)
                        .into(selectGroupImage)
                }.addOnFailureListener { exception ->
                    Log.e("AdapterGroupTest", "Failed to get download URL: ${exception.message}")
                }

                txtSelectGroupName.setText(group.groupName)
                var isSelected = selectedGroupIds.contains(group.groupId)
                imgRadioButtonGroup.visibility = if (isSelected) View.VISIBLE else View.GONE

                cardRadioButtonGroup.setOnClickListener {
                    val newSelected = !isSelected
                    if (newSelected) {
                        adapter.selectedGroupIds.add(group.groupId)
                        imgRadioButtonGroup.visibility = View.VISIBLE
                        // Add selected group students to view model
                        viewModel.addSelectedGroupStudents(group.groupId)
                    } else {
                        adapter.selectedGroupIds.remove(group.groupId)
                        imgRadioButtonGroup.visibility = View.GONE
                        // Remove selected group students from view model
                        viewModel.removeSelectedGroupStudents(group.groupId)
                    }
                    clickGroupListener.onPassArray(selectedGroupIds)
                    adapter.notifyItemChanged(adapterPosition)
                }
            }
        }
    }

    interface OnClickGroupClickListener{
        fun onPassArray(passedArray: ArrayList<String>)
    }
}
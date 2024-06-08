package com.kartik.tutordashboard.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kartik.tutordashboard.Data.Course
import com.kartik.tutordashboard.databinding.GpaCalculatorCourseListItemBinding

// [ListAdapater] implementation for the recyclerview
class CourseListAdapter(private val onCourseClicked: (Course) -> Unit) :
    ListAdapter<Course, CourseListAdapter.CourseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        return CourseViewHolder(
            GpaCalculatorCourseListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val current = getItem(position)

        holder.itemView.setOnClickListener {
            onCourseClicked(current)
        }
        holder.bind(current)
    }

    class CourseViewHolder(private var binding: GpaCalculatorCourseListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            binding.apply {
                courseName.text = course.courseName
                courseCredit.text = course.courseCredit.toString()
                courseGrade.text = course.courseGrade.toString()
            }
        }

    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Course>() {
            override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
                return oldItem.courseName == newItem.courseName
            }
        }
    }

}

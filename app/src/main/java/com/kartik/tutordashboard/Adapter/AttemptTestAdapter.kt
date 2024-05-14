package com.kartik.tutordashboard.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.databinding.CustomQuestionViewAdapterBinding

class AttemptTestAdapter(val context: Context, val questions: List<DataModel.Question>): RecyclerView.Adapter<AttemptTestAdapter.QuestionViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttemptTestAdapter.QuestionViewHolder {
        val binding = CustomQuestionViewAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttemptTestAdapter.QuestionViewHolder, position: Int) {
        holder.bind(questions[position])
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    class QuestionViewHolder(val binding: CustomQuestionViewAdapterBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(question: DataModel.Question){
            binding.apply {
                txtQuestion.text = question.text
                txtMarksSt.text = if(question.marks == 1){
                    "(${question.marks} Mark)"
                }else{
                    "(${question.marks} Marks)"
                }
                txtOption1.text = question.options[0]
                txtOption2.text = question.options[1]
                txtOption3.text = question.options[2]
                txtOption4.text = question.options[3]

                rbOption1.setOnClickListener {
                    question.selectedOption = question.options[0]
                    rbOption2.isChecked = false
                    rbOption3.isChecked = false
                    rbOption4.isChecked = false
                }
                rbOption2.setOnClickListener {
                    question.selectedOption = question.options[1]
                    rbOption1.isChecked = false
                    rbOption3.isChecked = false
                    rbOption4.isChecked = false
                }
                rbOption3.setOnClickListener {
                    question.selectedOption = question.options[2]
                    rbOption1.isChecked = false
                    rbOption2.isChecked = false
                    rbOption4.isChecked = false
                }
                rbOption4.setOnClickListener {
                    question.selectedOption = question.options[3]
                    rbOption1.isChecked = false
                    rbOption2.isChecked = false
                    rbOption3.isChecked = false
                }
            }
        }
    }

}
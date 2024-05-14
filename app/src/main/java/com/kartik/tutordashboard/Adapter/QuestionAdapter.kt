package com.kartik.tutordashboard.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.R

class QuestionAdapter(
    var questions: MutableList<DataModel.Question>,
    onclickListner: QuestionAdapter.onclickListner
): RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    val clickListner: QuestionAdapter.onclickListner =onclickListner
    class QuestionViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val edtQuestion: EditText = itemView.findViewById(R.id.edtQuestion)
        val edtOption1: EditText = itemView.findViewById(R.id.edtOption1)
        val edtOption2: EditText = itemView.findViewById(R.id.edtOption2)
        val edtOption3: EditText = itemView.findViewById(R.id.edtOption3)
        val edtOption4: EditText = itemView.findViewById(R.id.edtOption4)
        val edtMarks: EditText = itemView.findViewById(R.id.edtMarks)
        val spinnerCorrectOption: Spinner = itemView.findViewById(R.id.spinnerCorrectOption)
        val btnSaveQuestion: Button = itemView.findViewById(R.id.btnSaveQuestion)
        val btnEditQuestion: Button = itemView.findViewById(R.id.btnEditQuestion)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_question_create_layout, parent, false)
        return QuestionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {

        fun inputValidation(): Boolean{
            var op = true
            holder.apply {
                if(edtQuestion.text.toString().trim().isEmpty()){
                    edtQuestion.error = "Required Field"
                    op = false
                }
                if(edtOption1.text.toString().trim().isEmpty()){
                    edtOption1.error = "Required Field"
                    op = false
                }
                if(edtOption2.text.toString().trim().isEmpty()){
                    edtOption2.error = "Required Field"
                    op = false
                }
                if(edtOption3.text.toString().trim().isEmpty()){
                    edtOption3.error = "Required Field"
                    op = false
                }
                if(edtOption4.text.toString().trim().isEmpty()){
                    edtOption4.error = "Required Field"
                    op = false
                }
                if(edtMarks.text.toString().trim().isEmpty()){
                    edtMarks.error = "Required Field"
                    op = false
                }
                if (spinnerCorrectOption.selectedItem == "Answer"){
                    op = false
                }
                return op
            }
        }
        val question = questions[position]
        var correctAnswer = ""

        val options = arrayOf("Answer","Option 1", "Option 2", "Option 3", "Option 4")
        val adapter = object : ArrayAdapter<String>(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            options
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item (index 0)
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)

                // Customize styling for the first item
                if (position == 0) {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.gray)) // Set text color to gray
                } else {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.black)) // Set text color to black for other items
                }
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinnerCorrectOption.prompt = "Answer"
        holder.spinnerCorrectOption.adapter = adapter
        holder.spinnerCorrectOption.setSelection(0, false)

        holder.spinnerCorrectOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                correctAnswer = p0?.getItemAtPosition(p2) as String
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }

        holder.btnSaveQuestion.setOnClickListener {
           if(inputValidation()){
                holder.apply {
                    btnSaveQuestion.visibility = View.GONE
                    btnEditQuestion.visibility = View.VISIBLE


                    val question = edtQuestion.text.toString().trim()
                    val option1 = edtOption1.text.toString().trim()
                    val option2 = edtOption2.text.toString().trim()
                    val option3 = edtOption3.text.toString().trim()
                    val option4 = edtOption4.text.toString().trim()
                    val marks = edtMarks.text.toString().trim()
                    val answer = when(correctAnswer){
                        options[1] -> {option1}
                        options[2] -> {option2}
                        options[3] -> {option3}
                        options[4] -> {option4}
                        else -> {""}
                    }
                    //questionClickListener?.onSaveClicked(question, option1, option2, option3, option4, marks, answer)
                    clickListner.onSaveClicked(position, question, option1, option2, option3, option4, marks, answer)

                    if (question.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty() || marks == null || answer.isEmpty()) {
                        // Show error message (e.g., Toast)
                        return@setOnClickListener
                    }

                    edtQuestion.isEnabled = false
                    edtOption1.isEnabled = false
                    edtOption2.isEnabled = false
                    edtOption3.isEnabled = false
                    edtOption4.isEnabled = false
                    edtMarks.isEnabled = false
                    spinnerCorrectOption.isEnabled = false
                }
           }
        }

        holder.btnEditQuestion.setOnClickListener {
            holder.apply {
                btnEditQuestion.visibility = View.GONE
                btnSaveQuestion.visibility = View.VISIBLE

                edtQuestion.isEnabled = true
                edtOption1.isEnabled = true
                edtOption2.isEnabled = true
                edtOption3.isEnabled = true
                edtOption4.isEnabled = true
                edtMarks.isEnabled = true
                spinnerCorrectOption.isEnabled = true
            }

        }



    }

    interface onclickListner {
        fun onSaveClicked(position: Int, question: String, option1: String, option2: String, option3: String, option4: String, marks: String, answer: String)
    }
}
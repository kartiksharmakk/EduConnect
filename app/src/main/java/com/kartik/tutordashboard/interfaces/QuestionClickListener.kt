package com.kartik.tutordashboard.interfaces

import com.kartik.tutordashboard.Data.DataModel

interface QuestionClickListener {
    fun onQuestionInteraction(question: DataModel.Question, position: Int)
    fun onSaveClicked(question: String, option1: String, option2: String, option3: String, option4: String, marks: String, answer: String)
}
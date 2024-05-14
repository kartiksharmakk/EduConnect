package com.kartik.tutordashboard.Tutor

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartik.tutordashboard.Adapter.QuestionAdapter
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.Data.TestRepository
import com.kartik.tutordashboard.Data.TestViewModel
import com.kartik.tutordashboard.Data.TestViewModelFactory
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.databinding.FragmentCreateTestBinding
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.gson.Gson

class CreateTestFragment : Fragment(),  QuestionAdapter.onclickListner {
    lateinit var binding: FragmentCreateTestBinding
    lateinit var adapter: QuestionAdapter

    val firebaseDatabase = Firebase.database
    val testRepository = TestRepository(firebaseDatabase)
    val viewModelFactory = TestViewModelFactory(testRepository)
    val viewModel: TestViewModel by viewModels { viewModelFactory }
    var testId = ""
    var uid: String? = ""
    var questionsList = ArrayList<DataModel.Question>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateTestBinding.inflate(inflater, container, false)
        viewModel.testRepository = testRepository

        uid = Prefs.getUID(requireContext())
        viewModel.addEmptyQuestion()
        adapter = QuestionAdapter(viewModel.questions.value?: mutableListOf(), this)

        binding.rvTest.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTest.adapter = adapter
        viewModel.questions.observe(viewLifecycleOwner){questions->
            adapter.questions = questions
            adapter.notifyDataSetChanged()
        }
        binding.imgBackCreateTest.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnAssignTest.setOnClickListener {
            //viewModel.saveQuestionList(questionsList)
            if(binding.edtTestName.text.toString().trim().isNotEmpty()){
                val gson = Gson()
                val json = gson.toJson(questionsList)
                var bundle = Bundle()
                bundle.putString("questions_list",json)
                bundle.putString("testName",binding.edtTestName.text.toString().trim())
                viewModel.initNewTest(uid!!,binding.edtTestName.text.toString().trim())
                findNavController().navigate(R.id.allotTest,bundle)
            }else{
                binding.edtTestName.error = "Required field"
            }
        }
        /*binding.imgSaveCreateTest.setOnClickListener {
           showPopUp()
        }*/
        binding.imgAddQuestion.setOnClickListener {
            viewModel.addEmptyQuestion()
            adapter.notifyItemInserted(adapter.itemCount)
        }



        return binding.root
    }

    fun showPopUp(){
        val dialogInflater = LayoutInflater.from(requireContext())
        val view = dialogInflater.inflate(R.layout.custom_alert_dialog, null)

        val btnCreate: Button = view.findViewById(R.id.btnCreateCustomAlertDialog)
        val btnCancel: Button = view.findViewById(R.id.btnCancelAlertDialog1)

        val alertDialog = AlertDialog.Builder(requireContext()).setView(view)
            .setCancelable(false).create()

        /*
        btnCreate.setOnClickListener {
            testId = testRepository.generateTestId()
            viewModel.saveTestAndQuestions(testId,questionsList,uid)
            alertDialog.dismiss()
        }

         */
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }


    override fun onSaveClicked(
        position: Int,
        question: String,
        option1: String,
        option2: String,
        option3: String,
        option4: String,
        marks: String,
        answer: String
    ) {
        if(question != "" && option1 != "" && option2 != "" && option3 != "" && option4 != "" && answer != "" && marks != "" ) {
            val newQuestion = DataModel.Question(
                "${position}",
                question,
                listOf(option1, option2, option3, option4),
                answer,
                marks.toInt()
            )
            val existingQuestion = questionsList.find { it.questionId == newQuestion.questionId }
            if (existingQuestion == null) {
                questionsList.add(newQuestion)
            } else {
                existingQuestion.text = question
                existingQuestion.options = listOf(option1, option2, option3, option4)
                existingQuestion.correctOption = answer
                existingQuestion.marks = marks.toInt()
            }
        }
    }

}
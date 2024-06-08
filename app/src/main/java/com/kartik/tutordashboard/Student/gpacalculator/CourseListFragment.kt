
package com.kartik.tutordashboard.Student.gpacalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartik.tutordashboard.Adapter.CourseListAdapter
import com.kartik.tutordashboard.databinding.FragmentGpaCalculatorCourseListBinding

// Main fragment displaying details for all courses in the database
class CourseListFragment : Fragment() {

    private var _binding: FragmentGpaCalculatorCourseListBinding? = null
    private val binding get() = _binding!!

    /*
    private val viewModel: GpaCalculatorViewModel by activityViewModels {
        GpaCalculatorViewModelFactory(
            (activity?.application as CalculatorApplication).database.courseDao()
        )
    }

     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGpaCalculatorCourseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CourseListAdapter {
            val action =
                CourseListFragmentDirections.actionCourseListFragmentToCourseDetailFragment(it.id)
            this.findNavController().navigate(action)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter

        // Attach an observer on the allCourses list to update the  UI automatically when
        // the data changes
      //  viewModel.allCourses.observe(this.viewLifecycleOwner) { courses ->
     //       courses.let {
     //           adapter.submitList(it)
//
            }
      //      binding.gpaResult.text = viewModel.calculateGpa().toString()
        }
/*
        binding.floatingActionButton.setOnClickListener {
            val action = CourseListFragmentDirections.actionNavGpaCalculatorToAddCourseFragment(
                getString(R.string.add_fragment_title)
            )
            this.findNavController().navigate(action)
        }



 */
   // }

//}

package  com.kartik.tutordashboard.Student.gpacalculator

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kartik.tutordashboard.Data.Course
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.Student.StudentHome
import com.kartik.tutordashboard.TutorDashboardApplication
import com.kartik.tutordashboard.databinding.FragmentGpaCalculatorAddCourseBinding


class AddCourseFragment : Fragment() {

    // To share the view  model accross fragments
    private val viewModel: GpaCalculatorViewModel by activityViewModels {
        GpaCalculatorViewModelFactory(
            (activity?.application as TutorDashboardApplication).database.courseDao()
        )
    }

    // Binding object instance correspondding to the fragment_add_course.xml layout
    private var _binding: FragmentGpaCalculatorAddCourseBinding? = null
    private val binding get() = _binding!!

    lateinit var course: Course

    private val navigationArgs: CourseDetailFragmentArgs by navArgs()


    // Binds views with the passed in [course] information
    private fun bind(course: Course) {
        binding.apply {
            courseName.setText(course.courseName, TextView.BufferType.SPANNABLE)
//            courseCredit.getFocusables(0)
//            courseGrade.editText(course.courseGrade, TextView.BufferType.SPANNABLE)
            saveAction.setOnClickListener { updateCourse() }
        }
    }

    // Inserts the new course into the dataase and navigates up to the list fragment
    private fun addNewCourse() {
        if (isEntryValid()) {
            viewModel.addNewCourse(
                binding.courseName.text.toString(),
                binding.courseCredit.editText?.text.toString(),
                binding.courseGrade.editText?.text.toString(),
            )
        }
        val action = AddCourseFragmentDirections.actionAddCourseFragmentToCourseListFragment()
        findNavController().navigate(action)
    }

    // Updadtes an existing Course in the database and navigates up to list fragment
    private fun updateCourse() {
        if (isEntryValid()) {
            viewModel.updateCourse(
                this.navigationArgs.courseId,
                this.binding.courseName.text.toString(),
                this.binding.courseCredit.editText?.text.toString(),
                this.binding.courseGrade.editText?.text.toString()
            )
            val action = AddCourseFragmentDirections.actionAddCourseFragmentToCourseListFragment()
            findNavController().navigate(action)
        }
    }

    // Returns true if the EditTexts are not empty
    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.courseName.text.toString(),
            binding.courseCredit.editText?.text.toString(),
            binding.courseGrade.editText?.text.toString()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGpaCalculatorAddCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called when the view is created.
     * The id Navigation argument determines the edit course  or add new course.
     * If the id is positive, this method retrieves the information from the database and
     * allows the user to update it.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /// hide bottom nav here
        (activity as? StudentHome)?.hideBottomNavigation()

        val gradesAdapter = ArrayAdapter(requireContext(), R.layout.gpa_calculator_add_list_item, CREDITS)
        (binding.courseCredit.editText as? AutoCompleteTextView)?.setAdapter(gradesAdapter)

        val creditAdapter = ArrayAdapter(requireContext(), R.layout.gpa_calculator_add_list_item, GRADELETTERS)
        (binding.courseGrade.editText as? AutoCompleteTextView)?.setAdapter(creditAdapter)

        val id = navigationArgs.courseId
        if (id > 0) {
            viewModel.retrieveCourse(id).observe(this.viewLifecycleOwner) { selectedCourse ->
                course = selectedCourse
                bind(course)
            }
        } else {
            binding.saveAction.setOnClickListener {
                addNewCourse()
            }
        }
    }

    // Called before fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }

}
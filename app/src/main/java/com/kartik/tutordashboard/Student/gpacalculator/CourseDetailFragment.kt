
package  com.kartik.tutordashboard.Student.gpacalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kartik.tutordashboard.Data.Course
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.TutorDashboardApplication
import com.kartik.tutordashboard.databinding.FragmentGpaCalculatorCourseDetailBinding


// [CourseDetailFragment] displays the details of the  selected course
class CourseDetailFragment : Fragment() {

    private val navigationArgs: CourseDetailFragmentArgs by navArgs()
    lateinit var course: Course

    private var _binding: FragmentGpaCalculatorCourseDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GpaCalculatorViewModel by activityViewModels {
        GpaCalculatorViewModelFactory(
            (activity?.application as TutorDashboardApplication).database.courseDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGpaCalculatorCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Bind views with the passed in course data
    private fun bind(course: Course) {
        binding.apply {
            courseName.text = course.courseName
            courseCredit.text = course.courseCredit.toString()
            courseGrade.text = course.courseGrade.toString()
            deleteItem.setOnClickListener { showConfirmationDialog() }
//            editCourse.setOnClickListener { editCourse() }
        }
    }

    // Navigate to the Edit course screen.
//    private fun editCourse() {
//        val action = CourseDetailFragmentDirections.actionCourseDetailFragmentToAddCourseFragment(
//            getString(R.string.edit_fragment_title),
//            course.id
//        )
//        this.findNavController().navigate(action)
//    }

    /**
     * Displays an alert dialog to get the user's confirmation before deleting the item.
     */
    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteCourse()
            }
            .show()
    }

    // Deletes the current  course and navigates to the list fragment
    private fun deleteCourse() {
        viewModel.deleteCourse(course)
        findNavController().navigateUp()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.courseId

        /*
        * Retrieve the course details using the id.
        * Attach an observer on  the data (instead of polling for changes) and only update
        * the UI when the data actually changes.
         */
        viewModel.retrieveCourse(id).observe(this.viewLifecycleOwner) { selectedCourse ->
            course = selectedCourse
            bind(course)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
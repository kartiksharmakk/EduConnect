
package  com.kartik.tutordashboard.Student.gpacalculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kartik.tutordashboard.Data.Course
import com.kartik.tutordashboard.Data.CourseDao
import kotlinx.coroutines.launch

/**
 * View Model to keep a reference to the GPA Calculator repository and an up-to-date list of all courses.
 *
 */
class GpaCalculatorViewModel(private val courseDao: CourseDao) : ViewModel() {

    // Cache all courses from the database using LiveData
    val allCourses: LiveData<List<Course>> = courseDao.getCourses().asLiveData()

    fun calculateGpa(): Double {
        val gpa = 0.0
        var creditTimesGrade = 0.0
        var credits = 0

        allCourses.value?.forEach {
            credits += it.courseCredit
        }

        allCourses.value?.forEach {
            creditTimesGrade += it.courseCredit * letterGradeToNumber(it.courseGrade)
        }

        return String.format("%.2f", creditTimesGrade / credits).toDouble()
    }

    fun letterGradeToNumber(letterGrade: String): Double {
        var result: Double

        when (letterGrade) {
            "A" -> result = 4.0
            "A-" -> result = 3.67
            "B+" -> result = 3.33
            "B" -> result = 3.0
            "B-" -> result = 2.67
            "C+" -> result = 2.33
            "C" -> result = 2.0
            "C-" -> result = 1.67
            "D+" -> result = 1.33
            "D" -> result = 1.0
            "D-" -> result = 0.67
            else -> result = 0.0
        }

        return result
    }

    // Updates an existing course in the database
    fun updateCourse(
        courseId: Int,
        courseName: String,
        courseCredit: String,
        courseGrade: String
    ) {
        val updatedCourse = getUpdatedCourseEntry(courseId, courseName, courseCredit, courseGrade)
        updateCourse(updatedCourse)
    }

    // Launching a new coroutine to upddate a course in a non-blocking way
    private fun updateCourse(course: Course) {
        viewModelScope.launch {
            courseDao.update(course)
        }
    }

    /*
    * Called to update an existing entry in the database.
    * Returns an  instance of the [Course] entity class with the course info updated by the user
     */
    private fun getUpdatedCourseEntry(
        courseId: Int,
        courseName: String,
        courseCredit: String,
        courseGrade: String
    ): Course {
        return Course(
            id = courseId,
            courseName = courseName,
            courseCredit = courseCredit.toInt(),
            courseGrade = courseGrade
        )
    }

    // Inserts the new Course into database
    fun addNewCourse(courseName: String, courseCredit: String, courseGrade: String) {
        val newCourse = getNewCourseEntry(courseName, courseCredit, courseGrade)
        insertCourse(newCourse)
    }

    /*
    * Called to updated an existing entry in the database.
    * Returns an instance of the [Course] entity class with the course info updadtedd by the user
     */
    private fun getNewCourseEntry(
        courseName: String, courseCredit: String, courseGrade: String
    ): Course {
        return Course(
            courseName = courseName,
            courseCredit = courseCredit.toInt(),
            courseGrade = courseGrade
        )
    }

    // Launching a new coroutine to insert a course in a non-blocking way
    private fun insertCourse(course: Course) {
        viewModelScope.launch {
            courseDao.insert(course)
        }
    }

    // Launching a new coroutine to delete a course in a non-blocking way
    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            courseDao.delete(course)
        }
    }

    // Retrieve a course from the repository
    fun retrieveCourse(id: Int): LiveData<Course> {
        return courseDao.getCourse(id).asLiveData()
    }

    // Returns true if the EditTexts are not empty
    fun isEntryValid(
        courseName: String, courseCredit: String, courseGrade: String
    ): Boolean {
        return !(courseName.isBlank() || courseCredit.isBlank() || courseGrade.isBlank())
    }

}

// Factory class to instantiate the [ViewModel] isntance.
class GpaCalculatorViewModelFactory(private val courseDao: CourseDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GpaCalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GpaCalculatorViewModel(courseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package  com.kartik.tutordashboard.Student.gpacalculator

import android.app.Application
import com.kartik.tutordashboard.Data.CourseRoomDatabase

class CalculatorApplication : Application() {
    // Using by lazy so the database is only created when needed
    // rather than when the application starts
    val database: CourseRoomDatabase by lazy {
        CourseRoomDatabase.getDatabase(this)
    }
}
package com.kartik.tutordashboard.Student

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.databinding.ActivityStudentHomeBinding

class StudentHome : AppCompatActivity() {
    lateinit var binding: ActivityStudentHomeBinding
    lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_student_host_fragment)
        binding.studentBottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.st_home1->{
                    navController.navigate(R.id.studentHomeFragment)
                    true
                }
                R.id.st_test->{
                    navController.navigate(R.id.studentTestListFragment)
                    true
                }
                R.id.st_profile->{
                    navController.navigate(R.id.studentProfileFragment)
                    true
                }
                else->{
                    false
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
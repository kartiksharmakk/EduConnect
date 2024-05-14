package com.kartik.tutordashboard.Tutor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.databinding.ActivityTutorHomeBinding

class TutorHome : AppCompatActivity() {
    lateinit var binding: ActivityTutorHomeBinding
    lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_tutor_host_fragment)
        binding.tutorBottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.st_home -> {
                    navController.navigate(R.id.tutorHomeFragment)
                    true
                }
                R.id.createGroup -> {
                    navController.navigate(R.id.tutorCreateGroupFragment)
                    true
                }
                R.id.t_profile->{
                    navController.navigate(R.id.tutor_profile)
                    true
                }
                R.id.createTest->{
                    navController.navigate(R.id.tutorCreateTestFragment)
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
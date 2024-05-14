package com.kartik.tutordashboard.Student

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kartik.tutordashboard.Adapter.MyPagerAdapter
import com.kartik.tutordashboard.databinding.FragmentTestListStudentBinding
import com.google.android.material.tabs.TabLayoutMediator

class TestListStudent : Fragment() {
    lateinit var binding: FragmentTestListStudentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestListStudentBinding.inflate(inflater, container, false)
        binding.viewPager.adapter = MyPagerAdapter(requireActivity())
        TabLayoutMediator(binding.tabLayout, binding.viewPager){tab, position ->
            when(position){
                0 -> tab.text = "Pending"
                1 -> tab.text = "Attempted"
            }
        }.attach()

        return binding.root
    }
}
package com.kartik.tutordashboard.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kartik.tutordashboard.Tutor.AllotTestToGroupFragment
import com.kartik.tutordashboard.Tutor.AllotTestToStudentsFragment

class TestPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> AllotTestToGroupFragment()
            1 -> AllotTestToStudentsFragment()
            else -> AllotTestToGroupFragment()
        }
    }

}
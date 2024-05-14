package com.kartik.tutordashboard.Tutor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kartik.tutordashboard.databinding.FragmentGroupBinding

class GroupFragment : Fragment() {
    lateinit var binding: FragmentGroupBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupBinding.inflate(inflater, container, false)



        return binding.root
    }
}
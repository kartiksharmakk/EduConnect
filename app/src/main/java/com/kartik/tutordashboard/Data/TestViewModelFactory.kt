package com.kartik.tutordashboard.Data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TestViewModelFactory(private val testRepository: TestRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestViewModel(testRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
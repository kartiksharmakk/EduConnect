package com.kartik.tutordashboard.Data

object DataModel{
    data class TeacherModel(
        val uid: String = "",
        val name: String = "",
        val email: String = "",
        val groups: String = "",
        val tests: String = ""
    )

    data class UserCredentials(
        val uid: String = "",
        val name: String = "",
        val email: String = "",
        val countryCode: String = "",
        val phone: String = "",
        var image: String = "",
        val isVerified: Boolean = false
    )

    data class Group(
        val groupId: String = "",
        val groupName: String = "",
        val description: String = "",
        val subjectId: String = "",
        val tutorId: String = "",
        val coverImage: String = "",
        val displayImage: String = "",
        val students: List<String> = emptyList()
    )

    data class  Subjects(
        val subjectId: String = "",
        val name: String = ""
    )

    data class Students(
        val studentId: String = "",
        val name: String = "",
        val email: String = "",
        val countryCode: String = "",
        val phone: String = "",
        var image: String = ""
    )

    data class Messages(
        val messageId: String,
        val groupId: String,
        val senderId: String,
        val content: String,
        val timestamp: String
    )

    data class Test(
        val testId: String = "",
        val testName: String = "",
        val creatorId: String = "",
        val assignedTo: List<TestAssignedTo> = emptyList(),
        var questions: List<Question> = emptyList(),
        var totalMarks: Int = 0
    )

    data class Question(
        var questionId: String = "",
        var text: String = "",
        var options: List<String> = emptyList(),
        var correctOption: String = "",
        var marks: Int = 0,
        var selectedOption: String = ""
    )

    data class TestAssignedTo(
        val studentId: String = "",
        val hasAttempted: Boolean = false,
        val marks: Int = 0
    )

    data class Announcement(
        val title: String = "",
        val description: String = "",
        val url: String = ""
    )

}
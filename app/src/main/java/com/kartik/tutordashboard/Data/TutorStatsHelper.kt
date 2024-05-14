package com.kartik.tutordashboard.Data

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Counts(val groupCount: Int, val testCount: Int)

class TutorStatsHelper(private val context: Context, private val callback: (Counts) -> Unit) {
    private var groupCount = 0
    private var testCount = 0

    fun getTutorStats() {
        val tutorUid = Prefs.getUID(context)
        if (tutorUid != null) {
            val groupsRef = FirebaseDatabase.getInstance().getReference("Groups")
            val query = groupsRef.orderByChild("tutor_id").equalTo(tutorUid)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    groupCount = dataSnapshot.childrenCount.toInt()
                    checkCountsAndCallback()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error in getting group count for tutorId: $tutorUid\nError: $databaseError")
                }
            })

            val testsRef = FirebaseDatabase.getInstance().getReference("Tests")
            val testQuery = testsRef.orderByChild("creatorId").equalTo(tutorUid)

            testQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    testCount = dataSnapshot.childrenCount.toInt()
                    checkCountsAndCallback()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error in getting test count for tutorId: $tutorUid\nError: $databaseError")
                }
            })
        } else {
            Log.e(TAG,"Tutor id provided is null")
        }



    }

    private fun checkCountsAndCallback() {
        if (testCount != 0 && groupCount != 0) {
            callback.invoke(Counts(groupCount, testCount))
        }
    }

    companion object{
        val TAG: String = "TutorStatsHelper"
    }
}
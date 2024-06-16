package com.kartik.tutordashboard.Tutor

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.databinding.FragmentQRCodeScannerBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult


class QRCodeScannerFragment : Fragment() {
    lateinit var binding: FragmentQRCodeScannerBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var user: DataModel.UserCredentials
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQRCodeScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseDatabase = Firebase.database
        databaseReference = firebaseDatabase.getReference("User Details")
        startQRCodeScanner()
    }

    fun startQRCodeScanner(){
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR code")
        integrator.setBeepEnabled(true)
        integrator.setCameraId(0) // Use rear camera
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle QR code scan result
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            val uid = result.contents // Extract UID from QR code data
            openProfileFragment(uid)

        }
    }

    private fun openProfileFragment(uid: String) {
        val action = QRCodeScannerFragmentDirections.actionQRCodeScannerToTutorProfile(uid)
        findNavController().navigate(action)
    }

    /*fun fetchUserProfile(uid: String){
        val query = databaseReference.orderByChild("uid").equalTo(uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(userSnapshot in snapshot.children){
                        val userData = userSnapshot.getValue(DataModel.UserCredentials::class.java)
                        userData.uid

                    }
                }else{
                    Toast.makeText(requireContext(),"No user found, Try again", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
               
            }

        })
    }

     */
}
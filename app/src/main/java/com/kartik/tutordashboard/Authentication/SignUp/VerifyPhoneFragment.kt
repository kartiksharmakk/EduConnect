package com.kartik.tutordashboard.Authentication.SignUp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartik.tutordashboard.Adapter.OtpAdapter
import com.kartik.tutordashboard.Data.AuthenticationViewModel
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.Data.UserType
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.databinding.FragmentVerifyPhoneBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging

class VerifyPhoneFragment : Fragment() {
    lateinit var binding: FragmentVerifyPhoneBinding
    val viewModel: AuthenticationViewModel by activityViewModels<AuthenticationViewModel>()
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var tutorDatabaseReference: DatabaseReference
    lateinit var studentDatabaseReference: DatabaseReference
    private lateinit var storedVerificationId: String
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var pass: String
    private lateinit var countryCode: String
    private lateinit var phoneNumber: String
    private lateinit var userType: UserType
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerifyPhoneBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = Firebase.database
        databaseReference = firebaseDatabase.getReference("User Details")
        tutorDatabaseReference = firebaseDatabase.getReference("Tutor")
        studentDatabaseReference = firebaseDatabase.getReference("Student")
        Observer()
        binding.imgBackVerifyPhone.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.recyclerOtp.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerOtp.adapter = OtpAdapter(requireContext(), binding.recyclerOtp)
        binding.btnSignUp.setOnClickListener {
            signUp()
        }

        return binding.root
    }

    fun signUp(){
        val otp = (binding.recyclerOtp.adapter as OtpAdapter).getOtp()
        retrieveVerificationId()
        verifyPhoneWithOtp(storedVerificationId, otp)
    }
    fun signUpWithEmail(){
        val context = context ?: return
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
            if(it.isSuccessful){
                sendVerificationMail()
                Toast.makeText(context, "Sign up successful", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "Error " + it.exception, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendVerificationMail(){
        val user = auth?.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {
            try {
                if(it.isSuccessful){

                }else{

                }
            }catch (e: Exception){

            }
        }
    }
    fun retrieveVerificationId(){
        storedVerificationId = Prefs.getVerificationId(requireContext()).toString()
    }

    fun verifyPhoneWithOtp(verificationId: String, code: String){
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signinWithPhoneAuthCredentials(credential)
    }

    fun signinWithPhoneAuthCredentials(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) {task->
                if (task.isSuccessful) {
                    signUpWithEmail()
                    if(userType == UserType.TEACHER){
                        saveUserDetails()
                    }else if(userType == UserType.STUDENT){
                        saveStudentDetails()
                    }
                    findNavController().navigate(R.id.action_verifyPhoneFragment_to_signInFragment)
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // Show toast message for invalid OTP
                        Toast.makeText(requireContext(), "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Show toast message for other verification failures
                        Toast.makeText(requireContext(), "Verification failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    fun saveUserDetails(){
        val uid = "tr" + databaseReference.push().key!!
        val email1 = encodeEmail(email)
        val phoneWithCountryCode = countryCode + phoneNumber
        val image = "gs://tutordashboard-26c6a.appspot.com/Default_Resources/default_user_profile_image.png"
        val user = DataModel.UserCredentials(uid,name,email1, countryCode, phoneWithCountryCode, image,false )
        val tutor = DataModel.TeacherModel(uid, name, email1,"0", "0" )
        databaseReference.child(email1).setValue(user)
        databaseReference.child(email1).child("about").setValue("Hey there, I am using uplift.")
        tutorDatabaseReference.child(email1).setValue(tutor)
    }

    fun saveStudentDetails(){
        val uid = "st" + studentDatabaseReference.push().key!!
        val email1 = encodeEmail(email)
        val phoneWithCountryCode = countryCode + phoneNumber
        val image = "gs://tutordashboard-26c6a.appspot.com/Default_Resources/default_user_profile_image.png"
        val user = DataModel.UserCredentials(uid, name, email1, countryCode, phoneWithCountryCode, image, false)
        val student = DataModel.Students(uid, name, email1, countryCode, phoneWithCountryCode, image)
        databaseReference.child(email1).setValue(user)
        studentDatabaseReference.child(email1).setValue(student)
    }
    fun encodeEmail(email: String): String {
        return email.replace(".", "(dot)")
    }
    fun Observer(){
        viewModel.name.observe(viewLifecycleOwner){
            if (it != null){
                name = it
            }
        }
        viewModel.phone.observe(viewLifecycleOwner){
            if(it != null){
                binding.txtForgetPasswordDialog.setText(getString(R.string.verifyPhone)+"\n$it")
                phoneNumber = it
            }
        }
        viewModel.username.observe(viewLifecycleOwner){
            if(it != null){
                email = it
            }
        }
        viewModel.password.observe(viewLifecycleOwner){
            if(it != null){
                pass = it
            }
        }
        viewModel.countryCode.observe(viewLifecycleOwner){
            if(it != null){
                countryCode = it
            }
        }
        viewModel.userType.observe(viewLifecycleOwner){
            if(it != null){
                userType = it
            }
        }
    }

}
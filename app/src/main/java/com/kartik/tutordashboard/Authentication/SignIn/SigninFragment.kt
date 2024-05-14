package com.kartik.tutordashboard.Authentication.SignIn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kartik.tutordashboard.Functions.CommonFunctions
import com.kartik.tutordashboard.Functions.CommonFunctions.getPermissions
import android.Manifest
import android.content.Intent
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.Data.UserType
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.Student.StudentHome
import com.kartik.tutordashboard.Tutor.TutorHome
import com.kartik.tutordashboard.databinding.FragmentSigninBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging

class SigninFragment : Fragment() {
    lateinit var binding: FragmentSigninBinding
    lateinit var auth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var countryCode: String
    lateinit var phoneNumber: String
    var isPassVisible = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSigninBinding.inflate(inflater,container,false)
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = Firebase.database
        databaseReference = firebaseDatabase.getReference("User Details")

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getPermissions(requireActivity(), arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.POST_NOTIFICATIONS,
        ))

        binding.imgShowPassSignIn.setOnClickListener {
            showHidePassowrd()
        }
        binding.txtSignUp.setOnClickListener {
            findNavController().navigate(R.id.signupFragment)
        }
        binding.txtSendVerificationLink.setOnClickListener {
            sendVerificationLink()
        }
        binding.txtForgotSignIn.setOnClickListener {
            findNavController().navigate(R.id.forgotPasswordFragment)
        }
        binding.btnSignIn.setOnClickListener {
            login()
        }
        return binding.root
    }
    fun isEmpty(): Boolean{
        var empty = false
        if(binding.edtEmailSignIn.text.trim().toString().isEmpty()){
            empty = true
            binding.edtEmailSignIn.error = "Email is required"
        }
        if(binding.edtPasswordSignIn.text.trim().toString().isEmpty()){
            empty = true
            binding.edtPasswordSignIn.error = "Password is required"
        }
        return empty
    }
    fun login(){
        if(!isEmpty()){
            val email = binding.edtEmailSignIn.text.toString().trim()
            val password = binding.edtPasswordSignIn.text.toString().trim()
            signInRequest(email, password)
        }
    }

    fun signInRequest(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            try{
                if (it.isSuccessful){
                    checkVerificationStatus()
                }else{
                    Toast.makeText(requireContext(),"Please check your credentials",Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SignInRequest","Error: ${e.message}")
            }
        }
    }
    fun checkVerificationStatus(){
        auth.currentUser?.reload()?.addOnCompleteListener {
            try{
                if (it.isSuccessful){
                    if(auth.currentUser!!.isEmailVerified){
                        generateDeviceToken()
                        updateVerificationInDatabase(true)
                        Prefs.getLoggedIn(requireContext(),true)
                        val intentTutor = Intent(requireContext(), TutorHome::class.java)
                        val intentStudent = Intent(requireContext(), StudentHome::class.java)
                        isStudent { student ->
                            if(student){
                                startActivity(intentStudent)
                                requireActivity().finish()
                            }else{
                                startActivity(intentTutor)
                                requireActivity().finish()
                            }
                        }
                        findNavController().popBackStack()
                        Toast.makeText(requireContext(),"Sign In successful",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(requireContext(),"Please verify your email",Toast.LENGTH_SHORT).show()
                        binding.txtSendVerificationLink.visibility = View.VISIBLE
                    }
                }else{
                    Toast.makeText(requireContext(), "Failed to reload user", Toast.LENGTH_SHORT).show()

                }
            }catch (e: Exception){
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("CheckVerificationStatus","Error: ${e.message}")
            }
        }
    }

    fun sendVerificationLink(){
        val user = auth?.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {
            try {
                if(it.isSuccessful){
                    Toast.makeText(requireContext(),"Mail sent",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(),"Unable to send verification mail", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){
                Toast.makeText(requireContext(),"Error : ${e.message}",Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun encodeEmail(email: String): String {
        return email.replace(".", "(dot)")
    }

    fun isStudent(callback: (Boolean) -> Unit){
        val email = binding.edtEmailSignIn.text.toString().trim()
        val email1 = encodeEmail(email)
        val dbRef = databaseReference.child(email1).child("uid")
        var uid = ""
        dbRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                uid = snapshot.getValue().toString()
                val isStudent = uid?.startsWith("st") ?: false
                callback(isStudent)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("SigninFragment", "Cannot get user type")
                callback(false)
            }

        })
    }
    fun updateVerificationInDatabase(status: Boolean){
        val email = binding.edtEmailSignIn.text.toString().trim()
        val email1 = encodeEmail(email)
        updateSharedPreferences(email1)
        databaseReference.child(email1).child("verified").setValue(true)

    }

    fun updateSharedPreferences(email: String){
        Prefs.saveUserEmailEncoded(requireContext(), email)
        databaseReference.child(email).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists() && snapshot.hasChild("uid")){
                    val name = snapshot.child("name").value.toString()
                    val uid = snapshot.child("uid").value.toString()
                    Prefs.saveUID(requireContext(),uid)
                    Prefs.saveUsername(requireContext(),name)
                    if(uid.startsWith("st")){
                        Prefs.saveUserType(requireContext(),UserType.STUDENT)
                    }else{
                        Prefs.saveUserType(requireContext(),UserType.TEACHER)
                    }
                }else{
                    Log.d("SignInFragment","Error in updating uid to SharedPreferences (updateSharedPreferences())")
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    fun showHidePassowrd(){
        if(isPassVisible){
            binding.apply {
                imgShowPassSignIn.setImageResource(R.drawable.showpassword)
                edtPasswordSignIn.transformationMethod = PasswordTransformationMethod.getInstance()
                edtPasswordSignIn.setSelection(edtPasswordSignIn.text.length)
            }

        }else{
            binding.apply {
                imgShowPassSignIn.setImageResource(R.drawable.hidepassword)
                edtPasswordSignIn.transformationMethod = null
                edtPasswordSignIn.setSelection(edtPasswordSignIn.text.length)
            }
        }
        isPassVisible = !isPassVisible
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CommonFunctions.onRequestPermissionsResult(requireActivity(), requestCode, permissions, grantResults)
    }

    fun generateDeviceToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful){
                val email = Prefs.getUSerEmailEncoded(requireContext())!!
                val deviceToken = task.result
                databaseReference.child(email).child("deviceToken").setValue(deviceToken)
                Log.d("SignIn","Device Token: $deviceToken")
            }else{
                Log.d("SignIn","Device Token error")
            }
        }
    }


}
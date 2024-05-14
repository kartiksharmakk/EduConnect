package com.kartik.tutordashboard.Authentication.SignUp

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kartik.tutordashboard.Data.AuthenticationViewModel
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.Data.UserType
import com.kartik.tutordashboard.Functions.CommonFunctions.getToastShort
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.databinding.FragmentSignupBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SignupFragment : Fragment() {
    lateinit var binding: FragmentSignupBinding
    lateinit var auth: FirebaseAuth
    val viewModel: AuthenticationViewModel by activityViewModels<AuthenticationViewModel>()
    private var isPassVisible = false
    private var isConfirmPassVisible = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        binding.imgShowPasswordSignUp.setOnClickListener {
            showHidePassowrd()
        }
        binding.imgShowConfirmPasswordSignUp.setOnClickListener {
            showHideConfirmPassword()
        }
        binding.imgBackSignUp.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnVerifyNumber.setOnClickListener {
            generateOtp()
        }

        return binding.root
    }
    fun isEmpty(): Boolean{
        var emp = false
        if(binding.edtNameSignup.text.toString().trim().isEmpty()){
            emp = true
            binding.edtNameSignup.error = "Required field"
        }
        if(binding.edtEmailSignUp.text.toString().trim().isEmpty()){
            emp = true
            binding.edtEmailSignUp.error = "Required field"
        }
        if(binding.edtPhoneSignUp.text.toString().trim().isEmpty()){
            emp = true
            binding.edtPhoneSignUp.error = "Required field"
        }
        if(binding.edtPasswordSignUp.text.toString().trim().isEmpty()){
            emp = true
            binding.edtPasswordSignUp.error = "Required field"
        }
        if(binding.edtConfirmPasswordSignUp.text.toString().trim().isEmpty()){
            emp = true
            binding.edtConfirmPasswordSignUp.error = "Required field"
        }
        if(binding.edtPhoneSignUp.text.toString().trim().length != 10){
            emp = true
            binding.edtPhoneSignUp.error = "10 digits required"
        }
        if(binding.edtPasswordSignUp.text.toString() != "" &&
            binding.edtPasswordSignUp.text.toString().trim() != binding.edtConfirmPasswordSignUp.text.toString().trim()){
            emp = true
            binding.edtPasswordSignUp.error = "Passwords don't match"
            binding.edtConfirmPasswordSignUp.error = "Passwords don't match"
        }
        if (!binding.radioButtonStudent.isChecked && !binding.radioButtonTeacher.isChecked) {
            getToastShort(requireContext(), "Please select at least one option")
            emp = true
        }
        return emp
    }
    fun generateOtp(){
        if(!isEmpty()){
            val name = binding.edtNameSignup.text.toString().trim()
            val email = binding.edtEmailSignUp.text.toString().trim()
            val phone = binding.edtPhoneSignUp.text.toString().trim()
            val countryCode = binding.ccpSignUp.selectedCountryCodeWithPlus
            val pass = binding.edtPasswordSignUp.text.toString().trim()
            var userType = UserType.TEACHER
            if(binding.radioButtonTeacher.isChecked) {
                  userType =  UserType.TEACHER
            } else if (binding.radioButtonStudent.isChecked) {
                userType = UserType.STUDENT
            }
            startphonenumberVerification(countryCode+phone)
            viewModel.updateCredentials(name,email, countryCode, phone, pass, userType)
            findNavController().navigate(R.id.verifyPhoneFragment)
        }
    }

    fun startphonenumberVerification(phoneNumber: String){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
        }

        override fun onVerificationFailed(p0: FirebaseException) {
        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            Prefs.saveVerificationId(requireContext(),p0)
            val action = SignupFragmentDirections.actionSignupFragmentToVerifyPhoneFragment()
            findNavController().navigate(action)
        }
    }

    fun showHidePassowrd(){
        if(isPassVisible){
            binding.apply {
                imgShowPasswordSignUp.setImageResource(R.drawable.showpassword)
                edtPasswordSignUp.transformationMethod = PasswordTransformationMethod.getInstance()
                edtPasswordSignUp.setSelection(edtPasswordSignUp.text.length)
            }
        }else{
            binding.apply {
                imgShowPasswordSignUp.setImageResource(R.drawable.hidepassword)
                edtPasswordSignUp.transformationMethod = null
                edtPasswordSignUp.setSelection(edtPasswordSignUp.text.length)
            }
        }
        isPassVisible = !isPassVisible
    }

    fun showHideConfirmPassword(){
        if(isConfirmPassVisible){
            binding.apply {
                imgShowConfirmPasswordSignUp.setImageResource(R.drawable.showpassword)
                edtConfirmPasswordSignUp.transformationMethod = PasswordTransformationMethod.getInstance()
                edtConfirmPasswordSignUp.setSelection(edtConfirmPasswordSignUp.text.length)
            }
        }else{
            binding.apply {
                imgShowConfirmPasswordSignUp.setImageResource(R.drawable.hidepassword)
                edtConfirmPasswordSignUp.transformationMethod = null
                edtConfirmPasswordSignUp.setSelection(edtConfirmPasswordSignUp.text.length)
            }
        }
        isConfirmPassVisible = !isConfirmPassVisible
    }
}
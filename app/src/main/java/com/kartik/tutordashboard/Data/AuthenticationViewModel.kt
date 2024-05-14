package com.kartik.tutordashboard.Data
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthenticationViewModel: ViewModel() {
    private val mutableEmail = MutableLiveData<String>()
    private val mutablePhone = MutableLiveData<String>()
    private val mutablePassword = MutableLiveData<String>()
    private val mutableCountryCode = MutableLiveData<String>()
    private val mutableName = MutableLiveData<String>()
    private val mutableGender = MutableLiveData<String>()
    private val mutableUserType = MutableLiveData<UserType>()


    val username: LiveData<String> get() = mutableEmail
    val phone: LiveData<String> get() = mutablePhone
    val password: LiveData<String> get() = mutablePassword
    val countryCode: LiveData<String> get() = mutableCountryCode
    val name: LiveData<String> get() = mutableName
    val gender: LiveData<String> get() = mutableGender
    val userType: LiveData<UserType> get() = mutableUserType

    fun updateCredentials(name: String,email: String, code: String, phn: String, pass: String, userType: UserType){
        viewModelScope.launch {
            mutableName.value = name
            mutableEmail.value = email
            mutableCountryCode.value = code
            mutablePhone.value = phn
            mutablePassword.value = pass
            mutableUserType.value = userType
        }
        Log.d("ViewModel","UpdateCredentials : " +
                "\nEmail : ${username.value}" +
                "\ncode : ${countryCode.value}" +
                "\nPhone : ${phone.value}")
    }
}
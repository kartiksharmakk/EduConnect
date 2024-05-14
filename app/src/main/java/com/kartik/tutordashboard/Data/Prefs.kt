package com.kartik.tutordashboard.Data

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREF_NAME = "MY_SHARED_PREFERENCES"
    private lateinit var sharedPreferences: SharedPreferences

    private const val USER_IMAGE_URI = "image_uri"
    private const val USER_NAME = "name"
    private const val USER_EMAIL = "user_email"
    private const val KEY_USERNAME = "username"
    private const val UNIQUE_ID = "unique_id"
    private const val DEFAULT_USERNAME = ""
    private const val USER_TYPE = "user_type"
    private const val KEY_LOGGED_IN = "loggedIn"
    private const val VERIFICATION_ID_KEY = "verificationId"
    private const val DEVICE_TOKEN = "device_token"
    private const val TEST_COUNT = "test_count"
    private const val GROUP_COUNT = "group_count"
    private const val ALLOTED_TEST_COUNT = "alloted_test_count"
    private const val ATTEMPTED_TEST_COUNT = "attempted_test_count"


    var username: String
        get() = sharedPreferences.getString(KEY_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME
        set(value) = sharedPreferences.edit().putString(KEY_USERNAME, value).apply()

    private fun getPrefs(context: Context): SharedPreferences {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
        return sharedPreferences
    }

    fun saveUsername(context: Context, name: String){
        getPrefs(context).edit().putString(USER_NAME,name).apply()
    }
    fun getUsername(context: Context): String?{
        return getPrefs(context).getString(USER_NAME,null)
    }
    fun getLoggedIn(context: Context, isLogged: Boolean){
        getPrefs(context).edit().putBoolean(KEY_LOGGED_IN, isLogged).apply()
    }
    fun isLoggedIn(context: Context): Boolean?{
        return getPrefs(context).getBoolean(KEY_LOGGED_IN, false)
    }
    fun saveVerificationId(context: Context, verificationId: String) {
        getPrefs(context).edit().putString(VERIFICATION_ID_KEY, verificationId).apply()
    }

    fun getVerificationId(context: Context): String? {
        return getPrefs(context).getString(VERIFICATION_ID_KEY, null)
    }

    fun saveUID(context: Context, uid: String){
        getPrefs(context).edit().putString(UNIQUE_ID,uid).apply()
    }

    fun getUID(context: Context): String?{
        return getPrefs(context).getString(UNIQUE_ID, null)
    }

    fun saveUserImageURI(context: Context, uri: String){
        getPrefs(context).edit().putString(USER_IMAGE_URI, uri).apply()
    }
    fun getUserImageURI(context: Context): String?{
        return  getPrefs(context).getString(USER_IMAGE_URI, null)
    }

    fun saveUserEmailEncoded(context: Context, email: String){
        getPrefs(context).edit().putString(USER_EMAIL, email).apply()
    }
    fun getUSerEmailEncoded(context: Context): String?{
        return getPrefs(context).getString(USER_EMAIL, null)
    }

    fun saveUserType(context: Context, type: UserType){
        getPrefs(context).edit().putString(USER_TYPE, type.toString()).apply()
    }
    fun getUserType(context: Context): String?{
        return getPrefs(context).getString(USER_TYPE, null)
    }

    fun saveDeviceToken(context: Context, token: String){
        getPrefs(context).edit().putString(DEVICE_TOKEN, token).apply()
    }
    fun getDeviceToken(context: Context): String?{
        return getPrefs(context).getString(DEVICE_TOKEN, null)
    }

    fun saveTestCount(context: Context, count: Int){
        getPrefs(context).edit().putInt(TEST_COUNT, count).apply()
    }
    fun getTestCount(context: Context): Int{
        return getPrefs(context).getInt(TEST_COUNT,0)
    }

    fun saveGroupCount(context: Context, count: Int){
        getPrefs(context).edit().putInt(GROUP_COUNT, count).apply()
    }
    fun getGroupCount(context: Context): Int{
        return getPrefs(context).getInt(GROUP_COUNT,0)
    }

    fun saveAllotedTestCount(context: Context, count: Int){
        getPrefs(context).edit().putInt(ALLOTED_TEST_COUNT, count).apply()
    }
    fun getAllotedTestCount(context: Context): Int{
        return getPrefs(context).getInt(ALLOTED_TEST_COUNT,0)
    }

    fun saveAttemptedTestCount(context: Context, count: Int){
        getPrefs(context).edit().putInt(ATTEMPTED_TEST_COUNT, count).apply()
    }
    fun getAttemptedTestCount(context: Context): Int{
        return getPrefs(context).getInt(ATTEMPTED_TEST_COUNT,0)
    }

    fun clearPrefs() {
        sharedPreferences.edit().clear().apply()
    }
}
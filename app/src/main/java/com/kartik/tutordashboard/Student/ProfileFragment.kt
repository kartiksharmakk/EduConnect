package com.kartik.tutordashboard.Student

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.Shimmer
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.kartik.tutordashboard.Authentication.AuthActivity
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.Functions.CommonFunctions
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.Tutor.ProfileFragment
import com.kartik.tutordashboard.databinding.FragmentStudentProfileBinding
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Hashtable

class ProfileFragment : Fragment() {
    lateinit var binding: FragmentStudentProfileBinding
    lateinit var auth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    private var isVisible = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = Firebase.database
        databaseReference = firebaseDatabase.getReference("User Details")
        val uid = Prefs.getUID(requireContext())
        val qrCodeBitmap = generateQRCode(uid!!)
        getAboutSection()
        binding.apply {
            val name = Prefs.getUsername(requireContext())
            txtStudentNameProfile.setText(name)
            optionsStudentProfile.setOnClickListener {
                showPopUpOptions(it)
            }
            imgEditImageStudentProfile.setOnClickListener {
                checkPermission()
            }
            txtTestAttemptedCount.text = Prefs.getAttemptedTestCount(requireContext()).toString()
            txtStudentTestCount.text = Prefs.getAllotedTestCount(requireContext()).toString()

            txtGenerateQR.setOnClickListener {
                showQR(qrCodeBitmap!!)
            }
            imgEditAbout.setOnClickListener {
                showAboutPopUp()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDefaultProfileImage()
    }

    override fun onResume() {
        super.onResume()
        startImageShimmer()
        startStatusShimmer()
    }
    fun showPopUpOptions(view: View){
        val popUpMenu = PopupMenu(requireContext(), view)
        popUpMenu.menuInflater.inflate(R.menu.profile_options, popUpMenu.menu)

        popUpMenu.setOnMenuItemClickListener { item: MenuItem ->
            when(item.itemId){
                R.id.scan_qr->{
                    true
                }
                R.id.log_out->{
                    logoutAlertDialog()
                    true
                }
                else -> false
            }
        }
        popUpMenu.show()
    }

    private fun generateQRCode(uid: String): Bitmap? {
        val width = 500
        val height = 500
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = try {
            qrCodeWriter.encode(uid, BarcodeFormat.QR_CODE, width, height, Hashtable<EncodeHintType, String>())
        } catch (e: WriterException) {
            Log.d("ProfileFragment", "Exception in generating QR Code : ${e.message}")
            return null
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    private fun showQR(bMap: Bitmap){
        binding.apply {
            imgStudentProfileQR.setImageBitmap(bMap)
            if(isVisible){
                txtGenerateQR.setText("Show profile QR Code ")
                imgStudentProfileQR.visibility = View.GONE
            }else{
                txtGenerateQR.setText("Hide Profile QR")
                imgStudentProfileQR.visibility = View.VISIBLE
            }
            isVisible = !isVisible
        }
    }

    private fun checkPermission(){
        if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) != PackageManager.PERMISSION_GRANTED){
            requestStoragePermissions()
        }else{
            pickProfilePhoto()
        }
    }

    private fun requestStoragePermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestStoragePermissionsApi34()
        }
        else{
        requestPermissions(
            arrayOf(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED),
            ProfileFragment.STORAGE_PERMISSION_REQUEST_CODE
        )
        }
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val isGranted = it.value
            if (isGranted) {
                pickProfilePhoto()
                // Permission is granted
                // Handle the granted permission
            } else {
                showPermissionDeniedDialog()
                CommonFunctions.getToastShort(
                    requireContext(),
                    "Please provide storage permissions"
                )
                // Permission is denied
                // Handle the denied permission
            }
        }
    }

    private fun requestStoragePermissionsApi34() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionsToRequest = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }

            if (permissionsToRequest.isNotEmpty()) {
                storagePermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickProfilePhoto()
                }else{
                    showPermissionDeniedDialog()
                    CommonFunctions.getToastShort(
                        requireContext(),
                        "Please provide storage permissions"
                    )
                }
            }
        }
    }

    private fun showPermissionDeniedDialog(){
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Permissions Required")
            .setMessage("Permission required to access photos")
            .setPositiveButton("Open Settings"){dialog,_ ->
                openAppSettings()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }.setCancelable(false)
            .create()

        alertDialog.show()
        CommonFunctions.getToastShort(
            requireContext(),
            "Please provide storage permissions manually."
        )
    }

    private fun openAppSettings(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun getDefaultProfileImage(){
        val email = Prefs.getUSerEmailEncoded(requireContext())
        val emailRef = databaseReference.child(email!!)
        emailRef.child("image").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val imageUri = dataSnapshot.getValue(String::class.java)
                imageUri?.let {
                    val img = FirebaseStorage.getInstance().getReferenceFromUrl(imageUri)
                    try {
                        img.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(requireContext()).load(uri)
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.imgStudentImageProfile)
                            stopImageShimmer()
                        }.addOnFailureListener { exception ->
                            Log.e(
                                "StudentProfileFragment",
                                "Error loading profile image: ${exception.message}"
                            )
                        }
                    }catch (e: Exception){
                        Log.d("Image Loading exception",e.message.toString())
                    }
                } ?: run {
                    Log.e("StudentProfileFragment","Image URI is null")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("StudentProfileFragment", "Error fetching image URI from database: ${databaseError.message}")
            }
        })
    }

    private fun pickProfilePhoto(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type ="image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("ProfileFragment", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST_CODE) {
                Log.d("StudentProfileFragment", "Picked image successfully")
                data?.data?.let { uri ->
                    startCrop(uri)
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                Log.d("StudentProfileFragment", "UCrop.REQUEST_CROP")
                val resultUri = UCrop.getOutput(data!!)
                resultUri?.let {
                    Log.d("ProfileFragment", "Cropped image URI: $it")
                    showConfirmationDialog(it)
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("StudentProfileFragment", "Activity result cancelled")
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val error = UCrop.getError(data!!)
            Log.e("StudentProfileFragment", "Crop error: ${error?.localizedMessage}")
        }
    }

    private fun startCrop(uri: Uri){
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped"))
        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(90)
        }
        UCrop.of(uri, destinationUri)
            .withOptions(options)
            .withAspectRatio(1f, 1f)
            .start(requireContext(), this)

    }

    private fun uploadProfilePhoto(imageUri: Uri){
        try{
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val data = outputStream.toByteArray()
            val email = Prefs.getUSerEmailEncoded(requireContext())
            val fileName = "${email}}.jpg"//pending
            val profilePhotoRef = FirebaseStorage.getInstance().reference.child("profile_photos/$fileName")
            val uploadTask = profilePhotoRef.putBytes(data)
            uploadTask.addOnSuccessListener {taskSnapshot->
                profilePhotoRef.downloadUrl.addOnSuccessListener { uri->
                    //SharedPrefs
                    updateImageUriInDatabase(uri.toString())

                    Glide.with(requireContext()).load(uri).apply(RequestOptions.circleCropTransform())
                        .into(binding.imgStudentImageProfile)
                    CommonFunctions.getToastShort(requireContext(), "Profile photo uploaded")
                    stopImageShimmer()
                }
            }.addOnFailureListener{e ->
                Log.e("ProfileFragment", "Error uploading profile photo: ${e.message}")
            }
        }catch (e: Exception){
            Log.e("ProfileFragment", "Error uploading profile photo: ${e.message}")
        }
    }

    private fun updateImageUriInDatabase(imageUri: String){
        try{
            val email = Prefs.getUSerEmailEncoded(requireContext())
            val emailRef = databaseReference.child(email!!)
            emailRef.child("image").setValue(imageUri)
                .addOnSuccessListener {
                    Log.d("ProfileFragment", "Image URI updated successfully in database")
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Error updating image URI in database: ${e.message}")
                }
        }catch (e: Exception){
            Log.e("ProfileFragment", "Error updating image URI in database: ${e.message}")
        }
    }

    private fun showConfirmationDialog(imageUri: Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Image Upload")
            .setMessage("Do you want to upload this image?")
            .setPositiveButton("Yes") { _, _ ->
                // Upload the image
                uploadProfilePhoto(imageUri)
                startImageShimmer()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun showAboutPopUp(){
        val dialogInflater = LayoutInflater.from(requireContext())
        val view = dialogInflater.inflate(R.layout.custom_alert_dialog_with_edittext, null)

        val editText: EditText = view.findViewById(R.id.edtCustomAlertDialog)
        val btnUpdate: Button = view.findViewById(R.id.btnUpdateCustomAlertDialog)
        val btnCancel: Button = view.findViewById(R.id.btnCancelAlertDialog)

        val alertDialog = AlertDialog.Builder(requireContext()).setView(view)
            .setCancelable(false).create()
        editText.setText(binding.txtAboutStudentProfile.text.toString().trim())
        btnUpdate.isEnabled = false

        editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(p0: Editable?) {
                btnUpdate.isEnabled = p0?.isNotEmpty() ?: false
            }

        })
        btnUpdate.setOnClickListener {
            val aboutText = editText.text.toString().trim()
            updateAboutSection(aboutText)
            alertDialog.dismiss()
            startStatusShimmer()
        }
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    fun getAboutSection(){
        val email = Prefs.getUSerEmailEncoded(requireContext())
        val dbRef = databaseReference.child(email!!).child("about")
        dbRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val aboutText = snapshot.getValue(String::class.java)
                    binding.txtAboutStudentProfile.setText(aboutText)
                    stopStatusShimmer()
                }else{
                    Log.d("ProfileFragment","About text does not exist in the database")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        } )
    }

    fun updateAboutSection(text: String){
        val email = Prefs.getUSerEmailEncoded(requireContext())
        val dbRef = databaseReference.child(email!!)
        dbRef.child("about").setValue(text).addOnSuccessListener {
            CommonFunctions.getToastShort(requireContext(), "About section updated successfully")
            stopStatusShimmer()
        }.addOnFailureListener {
            Log.e("StudentProfileFragment", "Error updating about section.")
            CommonFunctions.getToastShort(requireContext(), "Failed to update about section")
        }
    }

    fun startImageShimmer(){
        val shimmer = Shimmer.AlphaHighlightBuilder().setBaseAlpha(0.8f).setHighlightAlpha(1.0f)
            .setRepeatCount(ValueAnimator.INFINITE)
            .build()
        binding.apply {
            shimmerProfileImage.setShimmer(shimmer)
            shimmerProfileImage.startShimmer()
            shimmerProfileImage.visibility = View.VISIBLE
        }

    }
    fun stopImageShimmer(){
        binding.apply {
            shimmerProfileImage.stopShimmer()
            shimmerProfileImage.visibility = View.GONE
        }
    }

    fun startStatusShimmer(){
        val shimmer = Shimmer.AlphaHighlightBuilder().setBaseAlpha(0.3f).setHighlightAlpha(0.9f)
            .setRepeatCount(ValueAnimator.INFINITE)
            .build()
        binding.apply {
            shimmerProfileStatus.setShimmer(shimmer)
            shimmerProfileStatus.startShimmer()
            shimmerProfileStatus.visibility = View.VISIBLE
        }
    }

    fun stopStatusShimmer(){
        binding.apply {
            shimmerProfileStatus.stopShimmer()
            shimmerProfileStatus.visibility = View.GONE
        }
    }
    private fun logout(){
        try{
            auth.signOut()
            Prefs.clearPrefs()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(intent)
            CommonFunctions.getToastShort(requireContext(), "Logged out")
            requireActivity().finish()
        }catch (e: Exception){
            CommonFunctions.getToastShort(requireContext(), "Error: ${e.message}")
        }
    }

    private fun logoutAlertDialog(){
        val dialogInflater = LayoutInflater.from(requireContext())
        val view = dialogInflater.inflate(R.layout.custom_alert_dialog, null)

        val title: TextView = view.findViewById(R.id.txtTitle)
        val text: TextView = view.findViewById(R.id.txtMessage)
        val btnCreate: Button = view.findViewById(R.id.btnCreateCustomAlertDialog)
        val  btnCancel: Button = view.findViewById(R.id.btnCancelAlertDialog1)

        val alertDialog = AlertDialog.Builder(requireContext()).setView(view).setCancelable(false).create()
        title.setText("Logging Out!")
        text.setText("Are you sure? Do you want to logout?")
        btnCreate.setText("Logout")
        btnCancel.setText("Cancel")
        btnCreate.setOnClickListener {
            logout()
            alertDialog.dismiss()
        }
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
    companion object{
        const val STORAGE_PERMISSION_REQUEST_CODE = 4001
        private const val PICK_IMAGE_REQUEST_CODE = 4002
    }
}
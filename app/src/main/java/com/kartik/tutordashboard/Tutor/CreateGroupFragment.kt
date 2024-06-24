package com.kartik.tutordashboard.Tutor

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kartik.tutordashboard.Data.DataModel
import com.kartik.tutordashboard.Data.Prefs
import com.kartik.tutordashboard.Data.TutorViewModel
import com.kartik.tutordashboard.Functions.CommonFunctions.getToastShort
import com.kartik.tutordashboard.Functions.CommonFunctions.sendNotificationToDevice
import com.kartik.tutordashboard.R
import com.kartik.tutordashboard.databinding.FragmentCreateGroupBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import java.io.File

class CreateGroupFragment : Fragment() {
    lateinit var binding: FragmentCreateGroupBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
     var displayImageUrl: Uri? = null
     var coverImageUrl: Uri? = null
    private var currentImageType: ImageType? = null
    private val viewModel: TutorViewModel by activityViewModels<TutorViewModel>()
    lateinit var selectedStudents: List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        firebaseDatabase = Firebase.database
        databaseReference = firebaseDatabase.getReference("Groups")
        binding.imgCreateGroupEditCoverImage.setOnClickListener {
            checkPermission(ImageType.COVER)
            currentImageType = ImageType.COVER
        }
        binding.imgCreateGroupEditDP.setOnClickListener {
            checkPermission(ImageType.DISPLAY)
            currentImageType = ImageType.DISPLAY
        }
        binding.imgCreateGroupAddMembers.setOnClickListener {
            findNavController().navigate(R.id.addStudents)
        }
        binding.btnCreateGroup.setOnClickListener {
            createGroup()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Observer()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun checkPermission(imageType: ImageType){
        if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            requestStoragePermissions(imageType)
        }else{
            pickImage(imageType)
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun requestStoragePermissions(imageType: ImageType){
        requestPermissions(
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
            when(imageType){
                ImageType.COVER -> COVER_STORAGE_PERMISSION_REQUEST_CODE
                ImageType.DISPLAY -> DISPLAY_STORAGE_PERMISSION_REQUEST_CODE
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            COVER_STORAGE_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImage(ImageType.COVER)
                }else{
                    showPermissionDeniedDialog()
                    getToastShort(
                        requireContext(),
                        "Please provide storage permissions"
                    )
                }
            }
            DISPLAY_STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage(ImageType.DISPLAY)
                } else {
                    showPermissionDeniedDialog()
                    getToastShort(
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
        getToastShort(
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

    private fun pickImage(imageType: ImageType){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, when(imageType){
            ImageType.COVER -> COVER_PICK_IMAGE_REQUEST_CODE
            ImageType.DISPLAY -> DISPLAY_PICK_IMAGE_REQUEST_CODE
        })
    }

    private fun startCrop(uri: Uri, imageType: ImageType){
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir,"cropped"))
        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(90)
        }
        val uCrop = UCrop.of(uri, destinationUri)
        uCrop.withOptions(options)
        uCrop.withAspectRatio(1f, 1f)
        uCrop.start(requireContext(), this)

        when(imageType) {
            ImageType.COVER -> coverImageUrl = uri
            ImageType.DISPLAY -> displayImageUrl = uri
            else -> displayImageUrl = uri
        }

    }

    private fun uploadImageToFirebaseStorage(grpId: String,imageUrl: Uri, imageType: ImageType) {
        val storageRef = FirebaseStorage.getInstance().reference
        val groupName = binding.edtCreateGroupName.text.toString().trim()
        val imageName = if (imageType == ImageType.COVER) "cover_image.jpg" else "display_image.jpg"
        val imgRef = storageRef.child("group_resources/$groupName/$imageName")

        val uploadTask = imgRef.putFile(imageUrl)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imgRef.downloadUrl.addOnSuccessListener { uri ->
                saveImageUrlToDatabase(grpId,imageType, uri.toString())
            }.addOnFailureListener { exception ->
                Log.e("CreateGroupFragment", "Error getting download URL: ${exception.message}")
            }
        }.addOnFailureListener { exception ->
            Log.e("CreateGroupFragment", "Error uploading image: ${exception.message}")
        }
    }
    private fun saveImageUrlToDatabase(grpId: String,imageType: ImageType, imageUrl: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Groups")

        val imageName = if (imageType == ImageType.COVER) "coverImage" else "displayImage"
        val imageUpdate = hashMapOf<String, Any>(imageName to imageUrl)

        databaseRef.child(grpId).updateChildren(imageUpdate)
            .addOnSuccessListener {
                Log.d("CreateGroupFragment", "Image URL saved to database successfully")
            }
            .addOnFailureListener { e ->
                Log.e("CreateGroupFragment", "Error saving image URL to database: ${e.message}")
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("CreateGroupFragment","onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                DISPLAY_PICK_IMAGE_REQUEST_CODE -> {
                    Log.d("CreateGroupFragment","Picked display image successfully")
                    data?.data?.let { uri ->
                        startCrop(uri, ImageType.DISPLAY)
                    }
                }
                COVER_PICK_IMAGE_REQUEST_CODE -> {
                    Log.d("CreateGroupFragment","Picked cover image successfully")
                    data?.data?.let { uri ->
                        startCrop(uri, ImageType.COVER)
                    }
                }
                UCrop.REQUEST_CROP -> {
                    Log.d("CreateGroupFragment","UCrop.REQUEST CROP")
                    val resultUri = UCrop.getOutput(data!!)
                    resultUri?.let {
                        Log.d("CreateGroupFragment","Cropped image URI: $it")
                        when (currentImageType) {
                            ImageType.COVER -> loadCroppedImageIntoView(it, binding.imgCreateGroupCoverImage)
                            ImageType.DISPLAY -> loadCroppedImageIntoView(it, binding.imgGroupDP)
                            else -> return
                        }
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("CreateGroupFragment","Activity result canceled")
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val error = UCrop.getError(data!!)
            Log.e("CreateGroupFragment","Crop error: ${error?.localizedMessage}")
        }
    }

    private fun loadCroppedImageIntoView(uri: Uri, imageView: ImageView) {
        Glide.with(requireContext())
            .load(uri)
            .apply(RequestOptions().placeholder(R.drawable.profile_icon)) // Placeholder image if needed
            .into(imageView)
    }
    private fun createGroup(){
        if(!isInputInvalid()){
            val groupId = "grp"+ databaseReference.push().key!!
            val name = binding.edtCreateGroupName.text.toString().trim()
            val description = binding.edtCreateGroupDescription.text.toString().trim()
            val subject = binding.edtCreateGroupSubject.text.toString().trim()
            val tutor_uid = Prefs.getUID(requireContext())!!
            val students = selectedStudents

            Log.e("CreateGroupFragment","GroupId : $groupId")
            Log.e("CreateGroupFragment","GroupId : $name")
            Log.e("CreateGroupFragment","GroupId : $description")
            Log.e("CreateGroupFragment","GroupId : $subject")
            Log.e("CreateGroupFragment","GroupId : $tutor_uid!!")
            Log.e("CreateGroupFragment","GroupId : $students")
            val group = DataModel.Group(groupId,name, description,subject,tutor_uid!!,"","", students)
            databaseReference.child(groupId).setValue(group).addOnSuccessListener {
                Log.d("CreateGroupFragment", "Group created successfully")
                displayImageUrl?.let { it1 ->
                    uploadImageToFirebaseStorage(groupId,
                        it1, ImageType.DISPLAY)
                }
                coverImageUrl?.let { it1 ->
                    uploadImageToFirebaseStorage(groupId,
                        it1, ImageType.COVER)
                }
                getToastShort(requireContext(),"Group created")
                val tokens = retrieveGroupMembersDeviceTokens(students)
                sendNotificationToAddedStudents(name,tokens)
            }.addOnFailureListener {
                Log.e("CreateGroupFragment", "Error creating group")
            }
        }

    }

    private fun isInputInvalid(): Boolean{
        var isInvalid = false
        if (binding.edtCreateGroupName.text.toString().trim().isEmpty()){
            binding.edtCreateGroupName.error = "Required field"
            isInvalid = true
        }
        if(binding.edtCreateGroupDescription.text.toString().trim().isEmpty()){
            binding.edtCreateGroupDescription.error = "Required field"
            isInvalid = true
        }
        if(binding.edtCreateGroupSubject.text.toString().trim().isEmpty()){
            binding.edtCreateGroupSubject.error = "Required field"
            isInvalid = true
        }
        if (displayImageUrl == null) {
            Toast.makeText(requireContext(),"Display image is required", Toast.LENGTH_SHORT).show()
            isInvalid = true
        }
        if (coverImageUrl == null) {
            Toast.makeText(requireContext(),"Cover image is required", Toast.LENGTH_SHORT).show()
            isInvalid = true
        }
        return isInvalid
    }

    fun retrieveGroupMembersDeviceTokens(studentIds: List<String>): List<String>{
        val deviceTokens = mutableListOf<String>()
        val userReference = firebaseDatabase.getReference("User Details")

        val deviceTokensQuery = userReference.orderByChild("uid")
        deviceTokensQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (id in studentIds) {
                    val userDataSnapshot = snapshot.children.find { it.child("uid").getValue(String::class.java) == id }
                    val deviceToken = userDataSnapshot?.child("deviceToken")?.getValue(String::class.java)
                    deviceToken?.let {
                        deviceTokens.add(it)
                    }
                }
                if (deviceTokens.isEmpty()) {
                    Log.e("CreateGroupFragment1", "No device tokens found for the provided student IDs")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CreateGroupFragment1", "Database query cancelled: ${error.message}")
            }
        })

        return deviceTokens
    }
    fun sendNotificationToAddedStudents(groupName: String, addedStudentTokens: List<String>){
        val title = "New Group Created"
        val message = "You have been added to the group: $groupName"
        for(token in addedStudentTokens){
            sendNotificationToDevice("e2FH7fV6Qw6zG4cYxQNcxT:APA91bHL7FKwI0fCP7wSOSDUavVC_UjYksqSgfYr7bOJTJKeP-RQQC65eoZU9dw0KjkWpCqRcfd7CqgrjQumCq6yvQwAQhn0jJybCI6h9h95GnMpfFxhJNkl2l2qY5K9Y7yetGHsayy_", title, message)
        }
        Log.d("CreateGroup","sending notification")
    }

    fun Observer(){
        viewModel.selectedStudentsCount.observe(viewLifecycleOwner){
            if(it != null){
                binding.txtCreateGroupMemberCount.text = it.toString()
            }
        }

        viewModel.selectedStudentIds.observe(viewLifecycleOwner){
            if (it != null){
                selectedStudents = it.toList()
                Log.d("CreateGroupFragment","$selectedStudents")
            }
        }
    }
    enum class ImageType{
        COVER,
        DISPLAY
    }
    companion object{
        private const val COVER_STORAGE_PERMISSION_REQUEST_CODE = 3001
        private const val DISPLAY_STORAGE_PERMISSION_REQUEST_CODE = 3002
        private const val PICK_IMAGE_REQUEST_CODE = 3003
        private const val COVER_PICK_IMAGE_REQUEST_CODE = 3004
        private const val DISPLAY_PICK_IMAGE_REQUEST_CODE = 3005
    }
}
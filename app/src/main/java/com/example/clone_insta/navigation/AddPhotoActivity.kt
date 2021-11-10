package com.example.clone_insta.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.clone_insta.R
import com.example.clone_insta.databinding.ActivityAddPhotoBinding
import com.example.clone_insta.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    var photoUri : Uri? = null
    val binding by lazy { ActivityAddPhotoBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //객체 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                binding.addphotoImage.setImageURI(photoUri)
            }
            else{
                finish()
            }
        }
    }
    fun contentUpload(){
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)
        //Callback method
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()

                //Insert downloadURL of image
                contentDTO.imageUrl = uri.toString()

                contentDTO.uid = auth?.currentUser?.uid

                contentDTO.userId = auth?.currentUser?.email

                contentDTO.explain = binding.addphotoEditExplain.text.toString()

                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()
            }
        }

        //Promise method -> Callback이 아닌 다른 업로드 방식, 골라서 사용
        /*storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            //Insert downloadURL of image
            contentDTO.imageUrl = uri.toString()

            contentDTO.uid = auth?.currentUser?.uid

            contentDTO.userId = auth?.currentUser?.email

            contentDTO.explain = binding.addphotoEditExplain.text.toString()

            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }*/
    }
}
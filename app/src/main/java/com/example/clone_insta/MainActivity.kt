package com.example.clone_insta

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.clone_insta.databinding.ActivityMainBinding
import com.example.clone_insta.navigation.*
import com.example.clone_insta.navigation.util.FcmPush
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    // 코틀린에서 이전에는 findViewById()를 사용하지 않고 레이아웃 요소의 id를 사용해 직접 변수처럼 접근할 수 있었지만,
    // 안정성 등 여러 문제가 있어 viewBinding이 대체로 등장했다. xml 레이아웃 파일을 생성하면 자동으로 해당 레이아웃의 바인딩 클래스가 생성되고,
    // 액티비티, 프래그먼트에서 바인딩 변수를 선언하고 바인딩 클래스로 초기화한 후 OnCreate()에서 setContentView(binding.root)를 전달하면
    // binding.{리소스 id}로 리소스 객체에 접근할 수 있다.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        registerPushToken()
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setToolbarDefault()
        when(item.itemId){
            R.id.action_home ->{
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, detailViewFragment).commit()
                return true
            }
            R.id.action_search ->{
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, gridFragment).commit()
                return true
            }
            R.id.action_add_photo ->{
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    startActivity(Intent(this, AddPhotoActivity::class.java))
                }
                return true
            }
            R.id.action_favorite_alarm ->{
                var alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, alarmFragment).commit()
                return true
            }
            R.id.action_accont ->{
                var userFragment = UserFragment()
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid", uid)
                userFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()
                return true
            }
        }
        return false
    }
    fun setToolbarDefault(){
        binding.toolbarUsername.visibility = View.GONE
        binding.toolbarBtnBack.visibility = View.GONE
        binding.toolbarTitleImage.visibility = View.VISIBLE
    }
    fun registerPushToken(){
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            task ->
            val token = task.result?.token
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val map = mutableMapOf<String, Any>()
            map["pushToken"] = token!!

            FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)
        }
    }

    override fun onStop() {
        super.onStop()
        FcmPush.instance.sendMessage("AwPd63dXQXbypbc4uQm42dlNPNn2", "hi", "test")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            var imageUrl = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storegRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storegRef.putFile(imageUrl!!).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storegRef.downloadUrl
            }.addOnSuccessListener { uri ->
                var map = HashMap<String, Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }
        }
    }
}
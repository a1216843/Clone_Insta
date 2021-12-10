package com.example.clone_insta.navigation.util

import com.example.clone_insta.navigation.model.PushDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.squareup.okhttp.*
import java.io.IOException

class FcmPush {
    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    // FCM 서버키는 파이어베이스 프로젝트 설정에서 클라우드 메시징 탭에서 확인 가능함 Google Developer console에서 발급하는 API키로는 안됨
    var serverKey = "AAAAOYBi1hw:APA91bHNzMw3P5N18Ruke4up9cHjCC_M5xeL92eTJD1fsuZXjhYp6aGqaFzssBnHcb5KrqDlOpcFavejacxgTFjm96d42UMcdz4qEGhaTm2tzKD2UhTOW2GrWjJPXKQni5Gx51-CvnyE"
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null
    // 싱글톤 패턴 선언으로 어디서나 serverKey를 사용할 수 있도록 함
   companion object{
        var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }
    fun sendMessage(destinationUid : String, title : String, message : String){
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                var token = task?.result?.get("pushToken").toString()

                var pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification.title = title
                pushDTO.notification.body = message

                var body = RequestBody.create(JSON, gson?.toJson(pushDTO))
                var request = Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "key="+serverKey)
                    .url(url)
                    .post(body)
                    .build()

                okHttpClient?.newCall(request)?.enqueue(object : Callback{
                    override fun onFailure(request: Request?, e: IOException?) {

                    }

                    override fun onResponse(response: Response?) {
                        println("푸시 메시지 전송")
                        println(response?.body()?.string())
                    }

                })
            }
        }
    }
}
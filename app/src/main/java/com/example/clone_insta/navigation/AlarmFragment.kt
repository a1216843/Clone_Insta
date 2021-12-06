package com.example.clone_insta.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.clone_insta.R
import com.example.clone_insta.databinding.FragmentAlarmBinding
import com.example.clone_insta.databinding.ItemCommentBinding
import com.example.clone_insta.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AlarmFragment : Fragment() {
    // 액티비티와는 다르게 뷰 바인딩 시 LayoutInflater를 onCreateView에서 넘겨받기 때문에 lateinit으로 선언해두는 것이 편하다
    // 주의할 점은 액티비티와는 다르게 바인딩의 inflater()메서드에 세 개의 파라미터가 사용된다. ex) binding.inflate(LayoutInflater, container, false) 형태로 사용

    lateinit var binding:FragmentAlarmBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // LayoutInflater는 XML 리소스를 View로 반환하는 역할을 함 주로 사용하는 onCreate의 setContentView도 이러한 Inflater의 역할을 내부적으로 수행한다.
        // inflate(View 객체로 만들 XML, 객체화 된 View를 담을 레이아웃 or 컨테이너, 바로 인플레이션 할 것인지 여부)
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        binding.alarmfragmentRecyclerview.adapter = AlarmRecyclerviewAdapter()
        binding.alarmfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }
    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                alarmDTOList.clear()
                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot.documents){
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
            System.out.println(alarmDTOList.size)
            System.out.println(uid)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)

            return CustomViewHolder(ItemCommentBinding.bind(view))
        }
        inner class CustomViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder as CustomViewHolder

            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val url = task.result!!["image"]
                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(holder.binding.commentviewitemImageviewProfile)
                }
            }

            when(alarmDTOList[position].kind){
                0 -> {
                    var str_0 = alarmDTOList[position].userId + " " +getString(R.string.alarm_favorite)
                    holder.binding.commentviewitemTextviewProfile.text = str_0
                }
                1 -> {
                    var str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_comment) + " of " + alarmDTOList[position].message
                    holder.binding.commentviewitemTextviewProfile.text = str_0
                }
                2 -> {
                    var str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_follow)
                    holder.binding.commentviewitemTextviewProfile.text = str_0
                }
            }
            holder.binding.commentviewitemTextviewComment.visibility = View.INVISIBLE
        }

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

    }
}
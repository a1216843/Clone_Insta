package com.example.clone_insta.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clone_insta.R
import com.example.clone_insta.databinding.FragmentDetailBinding
import com.example.clone_insta.databinding.ItemDetailBinding
import com.example.clone_insta.navigation.model.AlarmDTO
import com.example.clone_insta.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment() {
    lateinit var binding: FragmentDetailBinding
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // LayoutInflater는 XML 리소스를 View로 반환하는 역할을 함 주로 사용하는 onCreate의 setContentView도 이러한 Inflater의 역할을 내부적으로 수행한다.
        // inflate(View 객체로 만들 XML, 객체화 된 View를 담을 레이아웃 or 컨테이너, 바로 인플레이션 할 것인지 여부)
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        binding.detailviewfragmentRecyclerview.adapter = DetailViewRecyclerViewAdapter()
        binding.detailviewfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()
        init {
            System.out.println("테스트 출력 123123")
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                if(querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                    System.out.println("테스트 출력 121212")
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            System.out.println("테스트 출력 1234")
            return CustomViewHolder(ItemDetailBinding.bind(view))
        }

        inner class CustomViewHolder(val binding: ItemDetailBinding) : RecyclerView.ViewHolder(binding.root)

        override fun getItemCount(): Int{
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder as CustomViewHolder

            System.out.println("테스트 출력:"+contentDTOs[0].userId)

            holder.binding.detailviewitemProfileTextview.text = contentDTOs!![position].userId

            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(holder.binding.detailviewitemImageviewContent)

            holder.binding.detailviewitemExplainTextview.text = contentDTOs!![position].explain

            holder.binding.detailviewitemFavoritecounterTextview.text = "Likes" + contentDTOs!![position].favoriteCount

            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(holder.binding.detailviewitemProfileImage)

            //button event
            holder.binding.detailviewitemFavoriteImageview.setOnClickListener {
                favoriteEvent(position)
                if(contentDTOs!![position].favorites.containsKey(uid)){
                    // like status
                    holder.binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite_border)
                }
                else{
                    holder.binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite)
                }
            }

            holder.binding.detailviewitemProfileImage.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }

            holder.binding.detailviewitemCommentImageview.setOnClickListener {
                var intent = Intent(activity, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                startActivity(intent)
            }
        }

        fun favoriteEvent(position: Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                }
                else{
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentDTO?.uid!!)
                }
                transaction.set(tsDoc, contentDTO)
            }
        }
        fun favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }
    }
}
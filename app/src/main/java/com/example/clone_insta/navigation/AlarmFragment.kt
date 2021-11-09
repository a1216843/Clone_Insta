package com.example.clone_insta.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.clone_insta.R
import com.example.clone_insta.databinding.FragmentAlarmBinding

class AlarmFragment : Fragment() {
    // 액티비티와는 다르게 뷰 바인딩 시 LayoutInflater를 onCreateView에서 넘겨받기 때문에 lateinit으로 선언해두는 것이 편하다
    // 주의할 점은 액티비티와는 다르게 바인딩의 inflater()메서드에 세 개의 파라미터가 사용된다. ex) binding.inflate(LayoutInflater, container, false) 형태로 사용

    lateinit var binding:FragmentAlarmBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // LayoutInflater는 XML 리소스를 View로 반환하는 역할을 함 주로 사용하는 onCreate의 setContentView도 이러한 Inflater의 역할을 내부적으로 수행한다.
        // inflate(View 객체로 만들 XML, 객체화 된 View를 담을 레이아웃 or 컨테이너, 바로 인플레이션 할 것인지 여부)
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }
}
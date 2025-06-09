package com.yrlee.tpcafelog.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.FragmentHomeBinding
import com.yrlee.tpcafelog.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {

    lateinit var binding: FragmentMypageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }
}
package com.yrlee.tpcafelog.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ActivityMainBinding
import com.yrlee.tpcafelog.ui.favorite.FavoriteFragment
import com.yrlee.tpcafelog.ui.home.HomeFragment
import com.yrlee.tpcafelog.ui.mypage.MypageFragment
import com.yrlee.tpcafelog.ui.review.ReviewFragment

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.bab.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }
        binding.bnv.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }

        supportFragmentManager.beginTransaction().add(R.id.fragment_container, HomeFragment()).commit()
        binding.bnv.setOnItemSelectedListener {
            when(it.itemId){
                R.id.bnv_home -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                R.id.bnv_review -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ReviewFragment()).commit()
                R.id.bnv_favorite -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FavoriteFragment()).commit()
                R.id.bnv_mypage -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MypageFragment()).commit()
            }
            true
        }
    }
}
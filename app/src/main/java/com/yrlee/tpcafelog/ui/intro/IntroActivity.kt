package com.yrlee.tpcafelog.ui.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ActivityIntroBinding
import com.yrlee.tpcafelog.ui.login.LoginActivity
import com.yrlee.tpcafelog.ui.main.MainActivity

class IntroActivity : AppCompatActivity() {

    val binding by lazy { ActivityIntroBinding.inflate(layoutInflater) }
    val preferences by lazy { getSharedPreferences("User", MODE_PRIVATE)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val isSet = preferences.getBoolean("isSet", false)
        startActivity(Intent(this, MainActivity::class.java))
//        if(!isSet){ // 앱을 처음 실행한 경우
//            startActivity(Intent(this, LoginActivity::class.java))
//        }else{
//            startActivity(Intent(this, MainActivity::class.java))
//        }
        finish()
    }
}
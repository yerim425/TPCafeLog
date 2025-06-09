package com.yrlee.tpcafelog.ui.intro

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ActivityIntroBinding
import com.yrlee.tpcafelog.ui.login.LoginActivity

class IntroActivity : AppCompatActivity() {

    val binding by lazy { ActivityIntroBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val kakaoId = getPreferences(MODE_PRIVATE).getString("kakao_id", null)
        kakaoId?.let{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }
}
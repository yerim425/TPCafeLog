package com.yrlee.tpcafelog.ui.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yrlee.tpcafelog.MyApplication
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ActivityIntroBinding
import com.yrlee.tpcafelog.ui.login.LoginActivity
import com.yrlee.tpcafelog.ui.main.MainActivity
import com.yrlee.tpcafelog.ui.start.StartActivity
import com.yrlee.tpcafelog.util.PrefUtils

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
        Handler(Looper.getMainLooper()).postDelayed({
            if(PrefUtils.getBoolean("isSet")){
                if(PrefUtils.getBoolean("isLoggedIn")){ // 카카오 로그인을 한 유저
                    PrefUtils.getBoolean("isProfileSet").let{
                        if(!it){ // 프로필 설정 안함
                            startActivity(Intent(this, StartActivity::class.java))
                            finish()
                        }else{ // 프로필 설정 완료
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                }else{ // 로그인 없이 시작하기를 한 유저
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }else{ // 앱을 처음 실행한 경우
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 1500)

    }
}
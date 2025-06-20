package com.yrlee.tpcafelog.ui.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityIntroBinding
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.UserInfoRequest
import com.yrlee.tpcafelog.model.UserInfoResponse
import com.yrlee.tpcafelog.ui.login.LoginActivity
import com.yrlee.tpcafelog.ui.main.MainActivity
import com.yrlee.tpcafelog.ui.start.StartActivity
import com.yrlee.tpcafelog.util.PrefUtils
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IntroActivity : AppCompatActivity() {
    val TAG = "intro activity"

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
        // test 계정
//        PrefUtils.putInt("user_id", 1)
//        PrefUtils.putBoolean("isSet", true)
//        PrefUtils.putBoolean("isLoggedIn", true)
//        PrefUtils.putBoolean("isProfileSet", true)
//        PrefUtils.putString("nickname", "test")
//        PrefUtils.putString("level", "test")
//        PrefUtils.putString("kakao_id", "12345678")
        Handler(Looper.getMainLooper()).postDelayed({
            if(PrefUtils.getBoolean("isSet")){
                if(PrefUtils.getString("kakao_id").isNotEmpty()){ // 카카오 로그인을 한 유저
                    if(PrefUtils.getInt("user_id") != -1){ // 프로필 설정 완료
                        startActivity(Intent(this, MainActivity::class.java))
                    }else{
                        startActivity(Intent(this, StartActivity::class.java))
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
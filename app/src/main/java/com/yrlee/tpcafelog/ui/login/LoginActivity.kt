package com.yrlee.tpcafelog.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import com.yrlee.tpcafelog.MyApplication
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ActivityLoginBinding
import com.yrlee.tpcafelog.ui.intro.IntroActivity
import com.yrlee.tpcafelog.ui.main.MainActivity
import com.yrlee.tpcafelog.ui.start.StartActivity
import com.yrlee.tpcafelog.util.PrefUtils

class LoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    val TAG_KAKAO = "kakao login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 로그인 버튼
        binding.btnLogin.setOnClickListener {
            loginWithKakao()
        }

        // 로그인 없이 시작하기 버튼
        binding.tvStartWithoutLogin.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.start_without_login))
                .setMessage(getString(R.string.message_without_login))
                .setPositiveButton("확인"){ _, _ ->
                    PrefUtils.putBoolean("isSet", true)
                    PrefUtils.putBoolean("isLoggedIn", false)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .setNegativeButton("취소"){ dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    fun loginWithKakao(){
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG_KAKAO, "카카오 로그인 에러: ${error.message}", error)
            } else if (token != null) {
                Log.d(TAG_KAKAO, "토큰 받음: ${token.accessToken}")
                kakaoLoginSuccess(token)
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인
        if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            UserApiClient.instance.loginWithKakaoTalk(this){ token, error ->
                if(error != null){
                    Log.e(TAG_KAKAO, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (뒤로가기)
                    if(error is ClientError && error.reason == ClientErrorCause.Cancelled){
                        kakaoLoginFail()
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                }else if(token != null) {
                    kakaoLoginSuccess(token)
                }
            }
        }else{
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    fun kakaoLoginFail(){
        Toast.makeText(this, getString(R.string.message_login_fail), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    fun kakaoLoginSuccess(token: OAuthToken){
        Log.i(TAG_KAKAO, "카카오 로그인 성공")
        // 사용자 정보 sharedPreferences에 저장
        PrefUtils.putBoolean("isSet", true)
        PrefUtils.putBoolean("isLoggedIn", true)
        PrefUtils.putString("accessToken", token.accessToken)
        PrefUtils.putString("refreshToken", token.refreshToken)
        startActivity(Intent(this, StartActivity::class.java)) // 프로필 설정 화면으로 이동
        finish()
    }


}
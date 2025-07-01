package com.yrlee.tpcafelog.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import com.yrlee.tpcafelog.MyApplication
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityLoginBinding
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.UserInfoRequest
import com.yrlee.tpcafelog.model.UserInfoResponse
import com.yrlee.tpcafelog.ui.intro.IntroActivity
import com.yrlee.tpcafelog.ui.main.MainActivity
import com.yrlee.tpcafelog.ui.start.StartActivity
import com.yrlee.tpcafelog.util.PrefUtils
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    val TAG = "login activity"

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
                Log.e(TAG, "카카오 로그인 에러: ${error.message}", error)
            } else if (token != null) {
                Log.d(TAG, "토큰 받음: ${token.accessToken}")
                kakaoLoginSuccess(token)
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인
        if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            UserApiClient.instance.loginWithKakaoTalk(this){ token, error ->
                if(error != null){
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

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

    // 카카오 로그인 실패
    fun kakaoLoginFail(){
        Toast.makeText(this, getString(R.string.message_login_fail), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    // 카카오 로그인 성공
    fun kakaoLoginSuccess(token: OAuthToken){
        Log.i(TAG, "카카오 로그인 성공")

        // 카카오 프로필 정보 가져오기
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                requestKakaoUserInfoFail()
            } else if (user != null) {
                if(user.id == null){
                    requestKakaoUserInfoFail()
                }else{
                    Log.d(TAG, "카카오 사용자 정보 로딩 완료 ${user.id}")
                    PrefUtils.putString("kakao_id", user.id.toString())
                    PrefUtils.putBoolean("isSet", true)
                    val name = user.kakaoAccount?.profile?.nickname
                    val profileImageUrl = user.kakaoAccount?.profile?.profileImageUrl

                    requestMyUserInfo(user.id.toString(), name, profileImageUrl)
                }
            }
        }

    }

    // 카카오 유저 정보 요청 실패
    fun requestKakaoUserInfoFail(){
        Log.e(TAG, "사용자 정보 요청 실패")
        Toast.makeText(this, "정보 로딩에 실패했어요. 로그인을 다시 해주세요.", Toast.LENGTH_SHORT).show()
        PrefUtils.clearSharedPrefs()
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    fun requestMyUserInfo(id: String, name: String?, profileImageUrl: String?){
        // 카카오 아이디로 유저 검색
        val kakao_id = id
        val request = UserInfoRequest(kakao_id)
        val call = RetrofitHelper.getMyService().getUserInfo(request)
        call.enqueue(object : Callback<MyResponse<UserInfoResponse?>> {
            override fun onResponse(
                call: Call<MyResponse<UserInfoResponse?>>,
                response: Response<MyResponse<UserInfoResponse?>>
            ) {
                if(response.isSuccessful){
                    val body = response.body()
                    body?.status?.let{
                        when(it){
                            200 -> { // 유저 정보 있음
                                body.data?.let{
                                    PrefUtils.putInt("user_id", it.user_id)
                                    PrefUtils.putString("name", it.name)
                                    PrefUtils.putInt("level", it.level)
                                    PrefUtils.putString("title", it.title)
                                    PrefUtils.putString("img_url", it.img_url)
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    finish()
                                }
                            }
                            204 -> { // 유저 정보 없음 -> 프로필 설정 화면으로 이동
                                val intent = Intent(this@LoginActivity, StartActivity::class.java)
                                intent.putExtra("name", name)
                                intent.putExtra("profileImageUrl", profileImageUrl)
                                startActivity(intent)
                                finish()
                            }
                            else -> {
                            }
                        }
                    }
                }else {
                    Log.d(TAG, response.errorBody()?.string() ?: "errorBody is null")
                    startActivity(Intent(this@LoginActivity, IntroActivity::class.java))
                    finish()
                }
            }

            override fun onFailure(call: Call<MyResponse<UserInfoResponse?>>, t: Throwable) {
                Log.e(TAG, "${t.message}")
            }
        })
    }

}
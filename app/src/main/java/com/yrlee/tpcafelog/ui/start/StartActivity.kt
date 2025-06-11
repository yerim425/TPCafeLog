package com.yrlee.tpcafelog.ui.start

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.kakao.sdk.user.UserApiClient
import com.yrlee.tpcafelog.MyApplication
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.data.remote.RetrofitService
import com.yrlee.tpcafelog.databinding.ActivityStartBinding
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.User
import com.yrlee.tpcafelog.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date

class StartActivity : AppCompatActivity() {

    val binding by lazy { ActivityStartBinding.inflate(layoutInflater) }

    val TAG = "start activity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        requestKakaoUserInfo()
    }

    fun requestKakaoUserInfo(){ // 사용자의 카카오 정보 요청

        // 카카오 프로필 정보 가져오기
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패 ${error.message}", error)
                Toast.makeText(this, "로그인을 다시 해주세요.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java)) // 재로그인 요청
                finish()
            } else if (user != null) {
                user.id?.let{ // 카카오 id 저장
                    MyApplication.putString("kakao_id", it.toString())
                }

                val nickname = user.kakaoAccount?.profile?.nickname
                val profileImageUrl = user.kakaoAccount?.profile?.profileImageUrl
                nickname?.let{
                    val trimmedNickname = if(it.length>20) nickname.take(20) + "..." else nickname
                    binding.inputLayoutName.editText!!.setText(trimmedNickname)
                }
                profileImageUrl?.let {
                    Glide.with(this)
                        .load(it)
                        .placeholder(R.drawable.ic_profile_default)
                        .into(binding.ivProfile)
                }
            }
        }
    }

    fun userInfoSet(id: String, nickname: String, profileImageUrl: String){
        // DB에 유저 정보 INSERT
        val retrofit = RetrofitHelper.getMyRetrofitInstance()
        val retrofitService = retrofit.create(RetrofitService::class.java)
        val date = SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(Date())
        val call = retrofitService.insertUser(
            User(kakao_id = id, name = nickname, profile_img_url = profileImageUrl, level = 1, created_at = date))
        call.enqueue(object : Callback<MyResponse<String>> {
            override fun onResponse(
                call: Call<MyResponse<String>>,
                response: Response<MyResponse<String>>
            ) {
                response.body()?.let{

                    //Toast.makeText(this@MainActivity, "${it.resultCode}", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<MyResponse<String>>, t: Throwable) {
                //Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("aaa", "${t.message}")
            }

        })



        // shared_prefs에 자주 사용할 데이터들 저장
        nickname.let { MyApplication.putString("nickname", it) } // 닉네임
        MyApplication.putInt("level", 1) // 레벨
    }
}
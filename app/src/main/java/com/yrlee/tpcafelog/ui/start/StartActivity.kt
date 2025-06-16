package com.yrlee.tpcafelog.ui.start

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.kakao.sdk.user.UserApiClient
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityStartBinding
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.UserItem
import com.yrlee.tpcafelog.ui.intro.IntroActivity
import com.yrlee.tpcafelog.ui.main.MainActivity
import com.yrlee.tpcafelog.util.PrefUtils
import com.yrlee.tpcafelog.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class StartActivity : AppCompatActivity() {

    val binding by lazy { ActivityStartBinding.inflate(layoutInflater) }
    var imgRealPath: String? = null // null == 기본이미지로 설정함
    val TAG = "start activity"

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ it ->
        if(it.resultCode == RESULT_OK){
            it.data?.data?.let{
                Glide.with(this).load(it).into(binding.ivProfile)
                lifecycleScope.launch {
                    imgRealPath = Utils.getRealPathFromUri(it)
                }

            }
        }
    }
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

        binding.ivProfile.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            resultLauncher.launch(intent)
        }

        binding.btnStart.setOnClickListener {
            val nickname = binding.inputLayoutName.editText!!.text
            if(nickname.isEmpty() || nickname.length > 20){
                Toast.makeText(this, "닉네임을 알맞게 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            requestCreateUser()
        }
    }

    fun requestKakaoUserInfo(){ // 사용자의 카카오 정보 요청
        binding.btnStart.isEnabled = false
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

                        // 백그라운드에서 실행할 비동기 쓰레드
                        lifecycleScope.launch {
                            imgRealPath = getRealPathFromUrl(it)
                        }
                    }
                    binding.btnStart.isEnabled = true
                }
            }
        }
    }

    fun requestKakaoUserInfoFail(){
        Log.e(TAG, "사용자 정보 요청 실패")
        Toast.makeText(this, "정보 로딩에 실패했어요. 로그인을 다시 해주세요.", Toast.LENGTH_SHORT).show()
        PrefUtils.clearSharedPrefs()
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    fun requestCreateUser(){
        // 서버에 유저 정보 추가
        val userInfo = UserItem(
            kakao_id = PrefUtils.getString("kakao_id"),
            name = binding.inputLayoutName.editText!!.text.toString().trim(),
            level = 1,
            points = 0,
        )

        val json = Gson().toJson(userInfo)
        val dataBody = Utils.jsonToRequestBody(json)
        var filePart: MultipartBody.Part? = null
        imgRealPath?.let{
            if(File(it).exists()) {
                Log.d(TAG, "Real Path: $imgRealPath")
                filePart = Utils.filePathToMultipartBody(it)
            }
            else Log.d(TAG, "이미지 파일 없음: $imgRealPath")
        }
        val call = RetrofitHelper.getMyService().postUserInfo(dataBody, filePart)
        call.enqueue(object : Callback<MyResponse<String>>{
            override fun onResponse(
                call: Call<MyResponse<String>>,
                response: Response<MyResponse<String>>
            ) {
                val body = response.body()
                if(response.isSuccessful){
                    Log.d(TAG, "응답 body: $body")
                    if(body?.status == 200){
                        val id = body.data?.toInt() ?: 0
                        saveUserInfoToPrefs(userInfo, id)
                        startActivity(Intent(this@StartActivity, MainActivity::class.java))
                        finish()
                    }
                }else{
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "에러 응답: $errorBody")
                }

            }
            override fun onFailure(call: Call<MyResponse<String>>, t: Throwable) {
                Log.e(TAG, "Error: ${t.message.toString()}")
            }

        })
    }

    fun saveUserInfoToPrefs(userInfo: UserItem, id: Int){
        // shared_prefs에 자주 사용할 데이터들 저장
        PrefUtils.putInt("user_id", id)
        PrefUtils.putBoolean("isProfileSet", true)
        PrefUtils.putString("nickname", userInfo.name)
        PrefUtils.putInt("level", userInfo.level)

    }

    // 코루틴에서 실행할 함수
    suspend fun getRealPathFromUrl(url: String): String = withContext(Dispatchers.IO){
        // 카카오 프로필 사진을 다운로드하고 real path 가져오기

        // Glide로 Bitmap 다운로드
        val bitmap = Glide.with(this@StartActivity)
            .asBitmap()
            .load(url)
            .submit()
            .get()

        // 파일로 저장
        val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
        val file = File(externalCacheDir, fileName)

        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        return@withContext file.absolutePath
    }
}
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
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityStartBinding
import com.yrlee.tpcafelog.model.ErrorResponse
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.UserAddRequest
import com.yrlee.tpcafelog.model.UserInfoResponse
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
        // 카카오 닉네임, 프로필 사진 set
        val name = intent.getStringExtra("name") ?: ""
        val imgUrl = intent.getStringExtra("profileImageUrl")
        binding.inputLayoutName.editText!!.setText(name)
        Glide.with(binding.root).load(imgUrl).placeholder(R.drawable.ic_profile_default).into(binding.ivProfile)
        lifecycleScope.launch {
            imgUrl?.let{
                imgRealPath = getRealPathFromUrl(it)
            }

        }

        // 프로필 사진 변경
        binding.ivProfile.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            resultLauncher.launch(intent)
        }

        // 시작하기 -> DB에 유저 정보 저장
        binding.btnStart.setOnClickListener {
            val nickname = binding.inputLayoutName.editText!!.text
            if(nickname.isEmpty() || nickname.length > 20){
                Toast.makeText(this, "닉네임을 알맞게 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            requestAddUser()
        }
    }


    // 서버에 유저 정보 추가
    fun requestAddUser(){

        val userInfo = UserAddRequest(
            kakao_id = PrefUtils.getString("kakao_id"),
            name = binding.inputLayoutName.editText!!.text.toString().trim()
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
        call.enqueue(object : Callback<MyResponse<UserInfoResponse>>{
            override fun onResponse(
                call: Call<MyResponse<UserInfoResponse>>,
                response: Response<MyResponse<UserInfoResponse>>
            ) {
                val body = response.body()
                if(response.isSuccessful){ // 200
                    if(body?.status == 200){
                        // 유저 저장 성공
                        val data = body.data!!
                        PrefUtils.putInt("user_id", data.user_id)
                        PrefUtils.putString("name", data.name)
                        PrefUtils.putInt("level", data.level)
                        PrefUtils.putString("title", data.title)
                        PrefUtils.putString("img_url", data.img_url)
                        startActivity(Intent(this@StartActivity, MainActivity::class.java))
                        finish()
                    }
                }else{
                    Log.e(TAG, response.errorBody()?.string() ?: "errorBody is null")
                }

            }
            override fun onFailure(call: Call<MyResponse<UserInfoResponse>>, t: Throwable) {
                Log.e(TAG, "Error: ${t.message.toString()}")
            }

        })
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
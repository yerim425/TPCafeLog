package com.yrlee.tpcafelog.ui.visit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityVisitCertifyBinding
import com.yrlee.tpcafelog.model.ErrorResponse
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.Place
import com.yrlee.tpcafelog.model.CafeItem
import com.yrlee.tpcafelog.model.PointUpdateResponse
import com.yrlee.tpcafelog.util.LocationUtils
import com.yrlee.tpcafelog.util.PrefUtils
import com.yrlee.tpcafelog.util.SuccessDialogFragment
import com.yrlee.tpcafelog.util.Utils
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class VisitCertifyActivity : AppCompatActivity(), VisitCafeSearchDialogFragment.OnCafeSelectedListener {
    val TAG = "visit certify activity"

    private var checkLocation = false // 인증하려는 카페가 내 위치의 100m 이내에 있는지 체크
    private var checkImage = false    // 이미지 선택했는지 확인
    private var myLocation: Location? = null
    private var imgRealPath: String? = null
    val binding by lazy { ActivityVisitCertifyBinding.inflate(layoutInflater) }
    lateinit var selectedCafe: Place

    private val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it){
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.message_require_location_permission))
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .create().show()
        }
    }

    private val imageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ it ->
        if(it.resultCode == RESULT_OK){
            it.data?.data?.let{ uri ->
                Glide.with(this).load(uri).into(binding.iv)
                binding.iv.visibility = View.VISIBLE
                lifecycleScope.launch {
                    imgRealPath = Utils.getRealPathFromUri(uri)
                    imgRealPath?.let{
                        if(File(it).exists()) checkImage = true
                    }
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

        // 퍼미션 확인
        val permissionResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionResult == PackageManager.PERMISSION_DENIED)
            permissionResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) // 퍼미션 요청
        else LocationUtils.requestMyLocation(this) {
            it?.let {
                myLocation = it
                binding.btnSearchCafe.isEnabled = true
            }
        }

        // 취소 버튼
        binding.toolbar.setNavigationOnClickListener {
            AlertDialog.Builder(this).setTitle(R.string.message_stop_visit_certify)
                .setPositiveButton(getString(R.string.yes), {_,_->
                    finish()
                })
                .create().show()
        }
        onBackPressedDispatcher.addCallback(this) {
            AlertDialog.Builder(this@VisitCertifyActivity)
                .setTitle(R.string.message_stop_visit_certify)
                .setPositiveButton(R.string.yes) { _, _ ->
                    finish()
                }
                .show()
        }

        // 카페 이름 검색 및 위치 인증 완료
        binding.btnSearchCafe.setOnClickListener {
            if (myLocation == null) {
                Toast.makeText(
                    this,
                    getString(R.string.message_bringing_up_current_location),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                VisitCafeSearchDialogFragment(myLocation!!).show(
                    supportFragmentManager,
                    "CafeSearchDialog"
                )
            }
        }

        // 이미지
        binding.btnCertifyImage.setOnClickListener {
            imageResultLauncher.launch(Intent(MediaStore.ACTION_PICK_IMAGES))
        }

        // 최종 방문 인증 요청 보내기
        binding.btnCertify.setOnClickListener {
            requestPostVisitCertify()
        }
    }

    override fun onCafeSelected(cafeInfo: Place) {
        binding.tvCafeName.text = cafeInfo.place_name
        selectedCafe = cafeInfo
        checkLocation = true
    }

    fun requestPostVisitCertify(){
        if(!checkLocation){
            Toast.makeText(this, getString(R.string.message_certify_my_location), Toast.LENGTH_SHORT).show()
            return
        }
        if(!checkImage){
            Toast.makeText(this, getString(R.string.message_certify_visit_image), Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressbar.visibility = View.VISIBLE

        // 서버에 방문 인증 데이터를 저장
        val visitInfo = CafeItem(
            user_id = PrefUtils.getInt("user_id"),
            place_id = selectedCafe.id,
            name = selectedCafe.place_name,
            address = selectedCafe.address_name.ifEmpty { selectedCafe.road_address_name },
            category = selectedCafe.category_name,
            lat = selectedCafe.latitude,
            lng = selectedCafe.longitude,
            place_url = selectedCafe.place_url,
            phone = selectedCafe.phone

        )
        val json = Gson().toJson(visitInfo)
        val dataPart = Utils.jsonToRequestBody(json)
        var filePart: MultipartBody.Part? = null
        imgRealPath?.let{
            if(File(it).exists()) {
                Log.d(TAG, "Real Path: $imgRealPath")
                filePart = Utils.filePathToMultipartBody(it)
            }
            else {
                Toast.makeText(this, "이미지 파일이 없어요. 댜시 선택해주세요.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "이미지 파일 없음: $imgRealPath")
                return
            }
        }
        val call = RetrofitHelper.getMyService().postVisitInfo(dataPart, filePart)
        call.enqueue(object : Callback<MyResponse<PointUpdateResponse>>{
            override fun onResponse(
                call: Call<MyResponse<PointUpdateResponse>>,
                response: Response<MyResponse<PointUpdateResponse>>
            ) {
                if(response.isSuccessful){
                    val body = response.body()
                    body?.let{
                        when(it.status){
                            200 ->{
                                it.data?.let { data -> SuccessDialogFragment(
                                    getString(R.string.message_success_visit_certify),
                                    selectedCafe.place_name,
                                    data
                                ){
                                    finish()
                                }.show(supportFragmentManager, "VisitSuccess") }
                            }
                            else -> {}
                        }
                    }
                }else {
                    Log.e(TAG, response.errorBody()?.string() ?: "errorBody is null")
                }
            }

            override fun onFailure(call: Call<MyResponse<PointUpdateResponse>>, t: Throwable) {
                Log.e(TAG, "${t.message}")
            }
        })

        binding.progressbar.visibility = View.GONE
    }
}
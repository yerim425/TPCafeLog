package com.yrlee.tpcafelog.ui.review

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityReviewAddBinding
import com.yrlee.tpcafelog.model.CafeItem
import com.yrlee.tpcafelog.model.HashTagItem
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.PhotoItem
import com.yrlee.tpcafelog.model.VisitCafeInfoItem
import com.yrlee.tpcafelog.ui.visit.VisitCafeSearchDialogFragment
import com.yrlee.tpcafelog.util.PrefUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewAddActivity : AppCompatActivity(), VisitedCafeSelectDialogFragment.OnCafeSelectedListener {
    val TAG = "review add activity"
    val binding by lazy { ActivityReviewAddBinding.inflate(layoutInflater) }
    lateinit var hashtagAdapter: ReviewHashtagAdapter
    var visitedCafes: List<VisitCafeInfoItem>? = null
    var selectedCafe : VisitCafeInfoItem? = null
    var checkedHashtags : List<HashTagItem>? = null
    lateinit var reviewPhotoAdapter: ReviewPhotoAdapter
    val photoList = mutableListOf<PhotoItem>()
    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == RESULT_OK) {
            val clipData = result.data?.clipData
            val singleUri = result.data?.data

            val newUris = mutableListOf<Uri>()

            // 현재 photoList에 이미 들어있는 Local uri들만 필터링
            val existingUris = photoList
                .filterIsInstance<PhotoItem.Local>()
                .map { it.uri }

            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri: Uri = clipData.getItemAt(i).uri
                    if (!existingUris.contains(uri)) {
                        newUris.add(uri)
                    }
                }
            } else if (singleUri != null) {
                if (!existingUris.contains(singleUri)) {
                    newUris.add(singleUri)
                }
            }
            // 최대 6개 제한
            val remaining = 6 - photoList.size
            val newItems = newUris.take(remaining).map { PhotoItem.Local(it) }
            photoList.addAll(newItems)
            reviewPhotoAdapter.notifyDataSetChanged()
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
        requestHashTagNames()
        //requestVisitedCafes()


        // 카페 선택
        binding.btnSelectCafe.setOnClickListener {
            if(visitedCafes == null){
                Toast.makeText(this, getString(R.string.message_bringing_up_visited_cafes), Toast.LENGTH_SHORT).show()
            }else{
                if(visitedCafes?.size==0){
                    AlertDialog.Builder(this@ReviewAddActivity)
                        .setTitle(R.string.message_no_visited_cafe)
                        .setMessage(R.string.message_certify_visit_cafe_for_create_review)
                        .create().show()
                    finish()
                }else{
//                    visitedCafes?.let{
//                        VisitedCafeSelectDialogFragment(it).show(supportFragmentManager, "visitedcafeselectdialogfragment")
//                    }
                    //Toast.makeText(this, "${visitedCafes!!.get(0).place_name}", Toast.LENGTH_SHORT).show()
                    requestVisitedCafes()
                }
            }
        }

        // 사진 추가
        binding.btnAddPhoto.setOnClickListener {
            if(selectedCafe == null){
                Toast.makeText(this, "리뷰할 카페를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES).putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX,6)
            resultLauncher.launch(intent)
        }
    }


    // 해시태그 리스트 요청 및 리사이클러뷰 적용
    fun requestHashTagNames(){
        val call = RetrofitHelper.getMyService().getHastTagNames()
        call.enqueue(object : Callback<MyResponse<List<HashTagItem>>> {
            override fun onResponse(
                call: Call<MyResponse<List<HashTagItem>>>,
                response: Response<MyResponse<List<HashTagItem>>>
            ) {
                if(response.isSuccessful){
                    response.body()?.data?.let{
                        binding.recyclerviewHomeHashtag.adapter = ReviewHashtagAdapter(this@ReviewAddActivity, it)
                    }
                }
            }

            override fun onFailure(call: Call<MyResponse<List<HashTagItem>>>, t: Throwable) {
                Log.e(TAG, "${t.message}")
            }

        })
    }

    // 방문인증한 카페 리스트 요청하기
    fun requestVisitedCafes(){
        val userId = PrefUtils.getInt("user_id")
        val call = RetrofitHelper.getMyService().getVisitedCafeList(userId)
        call.enqueue(object : Callback<MyResponse<List<VisitCafeInfoItem>>>{
            override fun onResponse(
                call: Call<MyResponse<List<VisitCafeInfoItem>>>,
                response: Response<MyResponse<List<VisitCafeInfoItem>>>
            ) {
                if(response.isSuccessful){
                    val body = response.body()
                    when(body?.status){
                        200->{
                            visitedCafes = body.data
                            Toast.makeText(this@ReviewAddActivity, "${body.data?.get(0)?.place_name}", Toast.LENGTH_SHORT).show()
                        }
                        400 -> {
                            Log.d(TAG, response.errorBody()?.string() ?: "errorBody is null")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MyResponse<List<VisitCafeInfoItem>>>, t: Throwable) {
                Log.e(TAG, "${t.message}")
            }

        })
    }

    override fun onCafeSelected(visitInfo: VisitCafeInfoItem) {
        selectedCafe = visitInfo
    }

}
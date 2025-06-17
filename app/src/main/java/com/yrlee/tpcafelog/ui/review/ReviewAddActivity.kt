package com.yrlee.tpcafelog.ui.review

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityReviewAddBinding
import com.yrlee.tpcafelog.model.HashTagItem
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.PhotoItem
import com.yrlee.tpcafelog.model.ReviewAddResponse
import com.yrlee.tpcafelog.model.ReviewItem
import com.yrlee.tpcafelog.model.VisitCafeResponseItem
import com.yrlee.tpcafelog.model.VisitCafeAddResponse
import com.yrlee.tpcafelog.util.PrefUtils
import com.yrlee.tpcafelog.util.SuccessDialogFragment
import com.yrlee.tpcafelog.util.Utils
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ReviewAddActivity : AppCompatActivity(),
    VisitedCafeSelectDialogFragment.OnCafeSelectedListener {
    val TAG = "review add activity"
    val binding by lazy { ActivityReviewAddBinding.inflate(layoutInflater) }
    val userId = PrefUtils.getInt("user_id")
    var photoAdapter: ReviewPhotoAdapter?= null
    lateinit var hashtagAdapter: ReviewHashtagAdapter
    var selectedCafe: VisitCafeResponseItem? = null
    var checkedHashtags: List<HashTagItem>? = null
    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val clipData = result.data?.clipData // 이미지 여러개 선택한 경우
                val singleUri = result.data?.data // 이미지 하나만 선택한 경우

                val newUris = mutableListOf<Uri>()

                // 현재 photoList에 이미 들어있는 Local uri들만 필터링
                val existingUris =
                    photoAdapter!!.photoList.filterIsInstance<PhotoItem.Local>().map { it.uri }

                // 중복 이미지 제거
                var isOverlap = false
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        if (!existingUris.contains(uri)) {
                            newUris.add(uri)
                        }else isOverlap = true
                    }
                } else if (singleUri != null) {
                    if (!existingUris.contains(singleUri)) {
                        newUris.add(singleUri)
                    }else isOverlap = true
                }

                // 이미지 선택은 최대 6개 제한
                val remaining = 6 - photoAdapter!!.photoList.size
                val newItems = newUris.take(remaining).map { PhotoItem.Local(it) }
                photoAdapter!!.addPhotos(newItems)

                if(isOverlap) Toast.makeText(this@ReviewAddActivity.applicationContext, "중복된 이미지는 제외됩니다.", Toast.LENGTH_SHORT).show()
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

        // 카페 선택
        binding.btnSelectCafe.setOnClickListener {
            VisitedCafeSelectDialogFragment().show(
                supportFragmentManager,
                "visitedCafeSelectDialogFragment"
            )
        }

        // 사진 추가
        binding.btnAddPhoto.setOnClickListener {
            if (selectedCafe == null) {
                Toast.makeText(this@ReviewAddActivity.applicationContext, "리뷰할 카페를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            photoAdapter?.let{
                val intent =
                    Intent(MediaStore.ACTION_PICK_IMAGES).putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 6-it.itemCount)
                resultLauncher.launch(intent)
            }

        }

        // 제출하기
        binding.btnSubmit.setOnClickListener {
            // 카페 선택했는지 확인
            if (selectedCafe == null) {
                Toast.makeText(
                    this@ReviewAddActivity.applicationContext,
                    getString(R.string.message_select_cafe_for_review),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // 해시태그 선택했는지 확인
            checkedHashtags = hashtagAdapter.getCheckedList()
            checkedHashtags?.let {
                if (it.isEmpty()) {
                    Toast.makeText(
                        this@ReviewAddActivity.applicationContext,
                        getString(R.string.message_select_hashtags),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }
            requestReviewAdd()
        }

        // 뒤로 가기
        binding.toolbar.setNavigationOnClickListener {
            AlertDialog.Builder(this).setTitle(getString(R.string.message_stop_create_review))
                .setPositiveButton(getString(R.string.yes)){ _, _ ->
                    finish()
                }
                .create().show()
        }
        onBackPressedDispatcher.addCallback(this) {
            AlertDialog.Builder(this@ReviewAddActivity)
                .setTitle(R.string.message_stop_create_review)
                .setPositiveButton(R.string.yes) { _, _ ->
                    finish()
                }
                .show()
        }

        binding.edtReviewContent.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.nestedScrollView.post {
                    binding.nestedScrollView.smoothScrollTo(0, v.bottom)
                }
            }
        }
    }

    fun requestReviewAdd() {
        // DataBody 생성
        val review = ReviewItem(
            user_id = userId,
            place_id = selectedCafe!!.place_id,
            visit_id = selectedCafe!!.visit_id,
            content = binding.edtReviewContent.text.toString().trim(),
            rating = binding.ratingBar.rating,
            hashtag_ids = hashtagAdapter.getCheckedList().map { it.no }
        )
        val dataPart = Utils.jsonToRequestBody(Gson().toJson(review))

        val fileParts = photoAdapter!!.getPhotoUris().mapIndexed { _, uri ->
            val path = Utils.getRealPathFromUri(uri)
            Log.d(TAG, path)
            Utils.filePathToMultipartBodyForList(path)
        }
        val call = RetrofitHelper.getMyService().postReviewAdd(dataPart, fileParts)
        call.enqueue(object : Callback<MyResponse<ReviewAddResponse>> {
            override fun onResponse(
                call: Call<MyResponse<ReviewAddResponse>>,
                response: Response<MyResponse<ReviewAddResponse>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.let {
                        when (it.status) {
                            200 -> {
                                it.data?.let { data ->
                                    SuccessDialogFragment(
                                        getString(R.string.message_success_create_review),
                                        selectedCafe!!.place_name,
                                        data.user_point,
                                        data.gained_point
                                    ){
                                        finish()
                                    }.show(supportFragmentManager, "ReviewSuccess")
                                }
                            }
                            400 -> {
                                Log.d(TAG, it.data.toString())
                            }
                            else -> {}
                        }
                    }
                } else {
                    response.errorBody()?.let { Log.e(TAG, it.string()) }
                    Log.d(TAG, "response is not successful")
                }
            }

            override fun onFailure(call: Call<MyResponse<ReviewAddResponse>>, t: Throwable) {
                Log.e(TAG, "${t.message}")
            }
        })
    }


    // 해시태그 리스트 요청 및 리사이클러뷰 적용
    fun requestHashTagNames() {
        val call = RetrofitHelper.getMyService().getHastTagNames()
        call.enqueue(object : Callback<MyResponse<List<HashTagItem>>> {
            override fun onResponse(
                call: Call<MyResponse<List<HashTagItem>>>,
                response: Response<MyResponse<List<HashTagItem>>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        hashtagAdapter = ReviewHashtagAdapter(this@ReviewAddActivity, it)
                        binding.recyclerviewHomeHashtag.adapter = hashtagAdapter
                    }
                }
            }

            override fun onFailure(call: Call<MyResponse<List<HashTagItem>>>, t: Throwable) {
                Log.e(TAG, "${t.message}")
            }

        })
    }

    // 카페 리스트에서 카페 선택 시 콜백
    override fun onCafeSelected(visitCafeInfo: VisitCafeResponseItem) {
        selectedCafe = visitCafeInfo
        photoAdapter = ReviewPhotoAdapter(this, selectedCafe!!.img_url)
        binding.recyclerviewReviewPhoto.adapter = photoAdapter
        setPhotoNum(1)
        binding.tvCafeName.text = selectedCafe!!.place_name
    }

    fun setPhotoNum(num: Int){
        binding.tvPhotoNum.text = "${num}/6"
    }

}
package com.yrlee.tpcafelog.ui.review

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityReviewDetailBinding
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.ReviewDetailRequest
import com.yrlee.tpcafelog.model.ReviewDetailResponse
import com.yrlee.tpcafelog.model.ReviewLikeRequest
import com.yrlee.tpcafelog.model.ReviewLikeResponse
import com.yrlee.tpcafelog.util.PrefUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewDetailActivity : AppCompatActivity() {
    val TAG = "review detail activity"
    val binding by lazy { ActivityReviewDetailBinding.inflate(layoutInflater) }
    private var reviewId: Int = -1
    private var userId: Int? = PrefUtils.getInt("user_id")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        reviewId = intent.getIntExtra("reviewId", -1)
        if (reviewId == -1) {
            Toast.makeText(this, "리뷰가 존재하지 않습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
            finish()
        }
        requestReviewDetail()

        if (userId == -1) {
            userId = null
            binding.cbFavorite.isChecked = true
            binding.cbFavorite.isEnabled = false
        }

        binding.cbFavorite.setOnCheckedChangeListener { buttonView, isChecked ->
            requestReviewLike(isChecked)
        }
    }

    fun requestReviewDetail() {
        binding.progressbar.visibility = View.VISIBLE
        val data = ReviewDetailRequest(userId, reviewId)
        val call = RetrofitHelper.getMyService().getReviewDetail(data)
        call.enqueue(object : Callback<MyResponse<ReviewDetailResponse>> {
            override fun onResponse(
                call: Call<MyResponse<ReviewDetailResponse>>,
                response: Response<MyResponse<ReviewDetailResponse>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    data?.let {
                        // 카페 정보
                        binding.tvCafeName.text = it.cafeInfo.name
                        binding.tvAddress.text = it.cafeInfo.address

                        // 작성자 정보
                        Glide.with(binding.root).load(it.userInfo.user_img_url)
                            .placeholder(R.drawable.ic_app_logo).into(binding.ivUserProfile)
                        binding.tvUserLevel.text =
                            "Lv.${it.userInfo.user_level} ${it.userInfo.user_title}"
                        binding.tvUserName.text = it.userInfo.user_name

                        // 리뷰 정보
                        binding.tvUpdateAt.text =
                            if (it.is_modify == true) "${it.update_at}(수정됨)" else it.update_at
                        binding.tvReviewContent.text = "\"${it.content}\""
                        binding.tvLike.text = it.like_cnt.toString()

                    }
                } else {
                    Toast.makeText(
                        this@ReviewDetailActivity,
                        "현재 리뷰 정보를 불러올 수 없습니다. 잠시 후 다시 시도해 주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, response.errorBody()?.string() ?: "null")
                }
            }

            override fun onFailure(call: Call<MyResponse<ReviewDetailResponse>>, t: Throwable) {
                Log.e(TAG, "${t.message}")
            }
        })
        binding.progressbar.visibility = View.GONE

    }

    fun requestReviewLike(isChecked: Boolean) {
        if(userId == null) return
        val data = ReviewLikeRequest(userId, reviewId, isChecked)
        val call = RetrofitHelper.getMyService().updateReviewLike(data)
        call.enqueue(object : Callback<MyResponse<ReviewLikeResponse>> {
            override fun onResponse(
                call: Call<MyResponse<ReviewLikeResponse>>,
                response: Response<MyResponse<ReviewLikeResponse>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        if (it.is_like) {
                            binding.cbFavorite.isChecked = true
                        } else binding.cbFavorite.isChecked = false
                        binding.tvLike.text = it.like_cnt.toString()
                    }
                }else{
                    Log.e(TAG, response.errorBody()?.string() ?: "null")
                }
            }

            override fun onFailure(call: Call<MyResponse<ReviewLikeResponse>>, t: Throwable) {
                Log.e(TAG, "${t.message}")
            }

        })
    }
}
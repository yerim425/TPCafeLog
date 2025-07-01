package com.yrlee.tpcafelog.ui.review

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.local.OnReviewClickListener
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.FragmentReviewBinding
import com.yrlee.tpcafelog.model.MyResponse
import com.yrlee.tpcafelog.model.ReviewListItemResponse
import com.yrlee.tpcafelog.util.PrefUtils
import com.yrlee.tpcafelog.util.Utils
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewFragment : Fragment(), OnReviewClickListener {
    val TAG = "review fragment"

    lateinit var binding: FragmentReviewBinding
    var userId: Int? = null
    var page = 1
    var totalCnt = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReviewBinding.inflate(inflater, container, false)

        userId = PrefUtils.getInt("user_id")
        if(userId == -1) userId = null

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwnerLiveData.value?.let{
            it.lifecycleScope.launch {
                requestReviewList() // 리뷰 리스트 요청
            }
        }

        // 검색 리스너
        binding.edtSearchReview.setOnEditorActionListener { v, actionId, event ->
            viewLifecycleOwnerLiveData.value?.let{
                it.lifecycleScope.launch {
                    requestReviewList()
                }
            }
            binding.edtSearchReview.clearFocus()

            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtSearchReview.windowToken, 0)
            false
        }
    }

    fun requestReviewList(){
        binding.progressbar.visibility = View.VISIBLE

        val call = RetrofitHelper.getMyService().getReviewList(
            query = binding.edtSearchReview.text.toString().trim(),
            user_id = userId
        )
        call.enqueue(object : Callback<MyResponse<List<ReviewListItemResponse>>>{
            override fun onResponse(
                call: Call<MyResponse<List<ReviewListItemResponse>>>,
                response: Response<MyResponse<List<ReviewListItemResponse>>>
            ) {
                if(response.isSuccessful){
                    val body = response.body()
                    body?.let{
                        when(it.status){
                            200 -> {
                                it.data?.let{
                                    binding.recyclerviewReview.adapter = ReviewListAdapter(requireContext(), it, this@ReviewFragment)
                                }
                            }
                            400 -> {
                                Log.d(TAG, "400 -> ${it.data.toString()}")
                            }
                            else->{}
                        }
                    }
                }
                binding.progressbar.visibility = View.GONE
            }

            override fun onFailure(call: Call<MyResponse<List<ReviewListItemResponse>>>, t: Throwable) {
                Log.e(TAG, "error: ${t.message}")
                binding.progressbar.visibility = View.GONE
            }
        })
        binding.edtSearchReview.clearFocus()
    }

    // 리뷰 아이템 클릭 콜백
    override fun onItemClick(item: ReviewListItemResponse) {
        val intent = Intent(requireContext(), ReviewDetailActivity::class.java)
        intent.putExtra("reviewId", item.review_id)
        startActivity(intent)
    }

    override fun onLikeClick(item: ReviewListItemResponse, isChecked: Boolean) {
        TODO("Not yet implemented")
    }
}
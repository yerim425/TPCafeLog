package com.yrlee.tpcafelog.ui.map

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.FragmentCafeBottomSheetBinding
import com.yrlee.tpcafelog.model.CafeInfoRequest
import com.yrlee.tpcafelog.model.Place
import com.yrlee.tpcafelog.util.PrefUtils
import com.yrlee.tpcafelog.util.Utils
import kotlinx.coroutines.launch

class CafeBottomSheetFragment: BottomSheetDialogFragment() {

    companion object {
        private const val ARG_URL = "arg_url"
        fun newInstance(url: String): CafeBottomSheetFragment{
            val fragment = CafeBottomSheetFragment()
            val args = Bundle()
            args.putString(ARG_URL, url)
            fragment.arguments = args
            return fragment
        }

        private lateinit var url: String

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        url = arguments?.getString(ARG_URL) ?: ""
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_cafe_bottom_sheet, container, false)
        val webView = view.findViewById<WebView>(R.id.wv_cafe_detail)

        webView.settings.javaScriptEnabled = true

        // WebView 내부에서 링크 열도록 설정
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false // 모든 URL을 WebView에서 열기
            }
        }

        webView.loadUrl(url)  // 카카오 로컬 상세 페이지

//        webView.setOnTouchListener { v, event ->
//            // WebView가 최상단에 있으면 BottomSheet 드래그 가능
//            val canScrollUp = webView.canScrollVertically(-1) // true이면 위로 스크롤 가능
//            v.parent.requestDisallowInterceptTouchEvent(canScrollUp)
//            false
//        }

        return view
    }

//    override fun onStart() {
//        super.onStart()
//        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//        bottomSheet?.let {
//            val behavior = BottomSheetBehavior.from(it)
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        }
//    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        lifecycleScope.launch { requestCafeImageFromDB() }
//
//    }


//    suspend fun requestHomeCafeList(userId: Int, placeIds: List<String>){
//        try{
//            val request = CafeInfoRequest(userId, placeIds)
//            val response = RetrofitHelper.getMyService().getCafeInfos(request)
//            val status = response.status
//            when(status) {
//                200 -> {
//                    response.data?.let { data ->
//                        cafeAdapter.updateDBdata(data) // DB data로 카페 리스트 업데이트
//
//                        val nullIdx = data.indexOfFirst { it.visit_datas == null }
//                        Log.d(TAG, "nullIdx: ${nullIdx}")
//                        if(nullIdx > -1 && nullIdx < 5){ // 처음에는 화면에 보이는 리스트만 확인 이미지 검색
//                            viewLifecycleOwnerLiveData.value?.let{
//                                lifecycleScope.launch {
//                                    requestSearchNaverImage(0, data.size)
//                                }
//                            }
//                        }
//                    }
//                }
//                400 -> {
//                    Log.d(TAG, "${response.status}: ${response.data}")
//                }
//            }
//        }catch (e:Exception){
//            Log.e(TAG, "홈 데이터 요청 실패: ${e.message}")
//        }finally {
//            isLoadingGetHomeList = false
//        }
//    }
//
//    fun requestSearchNaverImage(startIdx: Int, size: Int){
//        for (i in startIdx until startIdx+size) {
//
//            val place = cafeAdapter.itemList.getOrNull(i) ?: continue
//            Log.d(TAG, "${startIdx}")
//            if (!place.isImgRequested && place.visitDatas == null) {
//                place.isImgRequested = true
//                val address =
//                    if (place.address_name.isEmpty()) place.address_name else place.road_address_name
//                val city = address.split(" ").firstOrNull() ?: ""
//                val query =
//                    city + " " + place.place_name + " " + getString(R.string.cafe)
//                viewLifecycleOwnerLiveData.value?.let {
//                    it.lifecycleScope.launch {
//                        val imageUrl = requestCafeImage(query)
//                        cafeAdapter.updateImage(i, imageUrl)
//                    }
//                }
//            }
//        }
//    }
//
//    suspend fun requestCafeImage(query: String): String? {
//        return try {
//            val response = RetrofitHelper.getNaverService().getSearchImage(query)
//            response.items.firstOrNull()?.link
//        } catch (e: Exception) {
//            Log.e(TAG, "이미지 요청 실패 : ${e.message}")
//            null
//        }
//    }

}
package com.yrlee.tpcafelog.ui.map

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
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

    var cafeInfo : Place?= null
    lateinit var binding: FragmentCafeBottomSheetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
//            cafeInfo = arguments?.getParcelable("cafeInfo", Place::class.java)
//        }else
//            cafeInfo = arguments?.getParcelable("cafeInfo")

        cafeInfo = (requireActivity() as MapCafeActivity).selectedCafe

        if(cafeInfo == null){
            Toast.makeText(requireContext(), "카페 정보를 불러오지 못했어요", Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCafeBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch { requestCafeImageFromDB() }

    }

    suspend fun requestCafeImageFromDB(){

        // 우선 디비에서 확인

        // 없으면 Place 정보들로 셋팅하고 네이버 이미지 요청

        try {
            val userId = PrefUtils.getInt("user_id")
            val request = CafeInfoRequest(userId, listOf(cafeInfo!!.id))
        }catch (E: Exception){

        }
    }

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
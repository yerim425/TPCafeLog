package com.yrlee.tpcafelog.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.yrlee.tpcafelog.BuildConfig
import com.yrlee.tpcafelog.MyApplication
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.data.remote.RetrofitHelper
import com.yrlee.tpcafelog.databinding.ActivityMapWebViewBinding
import com.yrlee.tpcafelog.model.CafeImageRequest
import com.yrlee.tpcafelog.util.LocationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import org.json.JSONArray

class MapWebViewActivity : AppCompatActivity() {
    val binding by lazy { ActivityMapWebViewBinding.inflate(layoutInflater) }

    var myLocation: Location = Location("default").apply {
        latitude = 37.550263
        longitude = 126.997083
    }
    @SuppressLint("JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.wv.webViewClient = object: WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // webview page가 만들어지면 카카오맵 초기화 하기
                LocationUtils.requestMyLocation(this@MapWebViewActivity) {
                    if (it == null) {
                        Toast.makeText(this@MapWebViewActivity, "위치를 가져오지 못했어요.", Toast.LENGTH_SHORT).show()
                    }else{
                        myLocation = it
                        lifecycleScope.launch { requestMyAddress(myLocation) }
//                      binding.wv.evaluateJavascript("initKakaoMap('${MyApplication.KAKAO_JS_API_KEY}', '${myLocation.latitude}', '${myLocation.longitude}');", null)
                        binding.wv.evaluateJavascript("initKakaoMap('${myLocation.latitude}', '${myLocation.longitude}');", null)
                        Log.d("MapWebViewActivity", "${myLocation.latitude} ${myLocation.longitude}")
                    }
                }
            }
            //lifecycleScope.launch {
//                // 내 위치 주소 얻기
//                requestMyAddress(myLocation?.latitude.toString(), myLocation?.longitude.toString())
//            }
        }
        binding.wv.webChromeClient = object : WebChromeClient(){
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d("here", "JS 로그: ${consoleMessage?.message()}")
                return true
            }
        }
        binding.wv.settings.javaScriptEnabled = true
        binding.wv.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")
        binding.wv.loadUrl("http://yrlee2025.dothome.co.kr/cafelog/cafelog_map.html")
        binding.wv.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

    }

    suspend fun requestMyAddress(myLocation: Location) {
        binding.tvCafeAddress.text = "카페"
        try {
            val response = RetrofitHelper.getKakaoService().getMyAddress( myLocation.longitude.toString(), myLocation.latitude.toString())
            val address = response.documents.firstOrNull()?.address?.region_3depth_name ?: ""
            withContext(Dispatchers.Main) {
                Log.d("map cafe activity", "$address 카페".trim())
                binding.tvCafeAddress.text = "$address 카페".trim()
            }
        } catch (e: Exception) {
            Log.e("map cafe activity", "error: ${e.message}")
        }
    }

    // 웹에서 호출할 함수 인터페이스
    inner class WebAppInterface(val context: Context){

        // 카페 id 배열 받아서 DB에 카페 이미지를 요청
//        @JavascriptInterface
//        fun sendPlaceIds(placeIdsJson: String){
//            Log.d("MapWebViewActivity", "sendPlaceIds")
//            // json 배열 파싱
//            val jsonArray = JSONArray(placeIdsJson)
//            val placeIds = mutableListOf<String>()
//            for(i in 0 until jsonArray.length()){
//                placeIds.add(jsonArray.getString(i))
//            }
//
//            // DB에 카페 이미지 요청
//            (context as MapWebViewActivity).lifecycleScope.launch {
//                try{
//                    val request = CafeImageRequest(placeIds)
//                    val response = RetrofitHelper.getMyService().getCafeImages(request)
//                    response.data?.let{
//                        if(it.isNotEmpty()) {
//                            Log.d("MapWebViewActivity", it.toString())
//                            val jsonArrayString = Gson().toJson(it) // it: List<CafeImageResponse>
//                            binding.wv.evaluateJavascript("updateCafeImages('$jsonArrayString')", null)
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.e("MapWebViewActivity", "getCafeImages 요청 실패 : ${e.message}")
//                }
//            }
//        }

        // 마커 클릭 시 상세 페이지 띄우기
        @JavascriptInterface
        fun showCafeDetail(placeUrl: String){
            Log.d("MapWebViewActivity", placeUrl)
            (context as AppCompatActivity).runOnUiThread{
                val bottomSheet = CafeBottomSheetFragment.newInstance(placeUrl)
                bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "CafeDetail")
            }

        }

    }
}
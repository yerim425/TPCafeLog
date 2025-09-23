package com.yrlee.tpcafelog.ui.map

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yrlee.tpcafelog.BuildConfig
import com.yrlee.tpcafelog.MyApplication
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ActivityMapWebViewBinding
import com.yrlee.tpcafelog.util.LocationUtils

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
                    }
                    view?.evaluateJavascript("initKakaoMap('${MyApplication.KAKAO_JS_API_KEY}', '${myLocation.latitude}', '${myLocation.longitude}');", null)
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
        binding.wv.addJavascriptInterface(WebAppInterface(this), "Android")
        binding.wv.loadUrl("file:///android_asset/map.html")
        binding.wv.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW


        LocationUtils.requestMyLocation(this) {
            if (it == null) {
                myLocation.latitude = 37.550263
                myLocation.longitude = 126.997083
                Toast.makeText(this, "위치를 가져오지 못했어요.", Toast.LENGTH_SHORT).show()
            }else{
                myLocation = it
            }
        }
    }

    // 웹에서 호출할 함수 인터페이스 ??
    inner class WebAppInterface(private val activity: MapWebViewActivity){

    }
}
package com.yrlee.tpcafelog.ui.home

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ActivityCafeDetailBinding
import com.yrlee.tpcafelog.model.Place

class CafeDetailActivity : AppCompatActivity() {
    val binding by lazy { ActivityCafeDetailBinding.inflate(layoutInflater) }
    lateinit var place: Place
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val placeStr: String? = intent.getStringExtra("place")
        placeStr?.also {
            place = Gson().fromJson(placeStr, Place::class.java)

            with(binding.webview) {
                // 웹뷰를 사용할 때 반드시 해야할 설정 3가지
                webViewClient = WebViewClient() // 현재 웹뷰안에서 웹문서를 열리도록..
                webChromeClient = WebChromeClient() // 웹문서안에서 경고창이나 팝업같은 것들이 발동하도록
                settings.javaScriptEnabled = true // 웹뷰는 기본적으로 보안 문제로 JS 동작을 막아놓았기에.. 허용하도록
                // 웹뷰에 장소의 세부정보 url 설정
                loadUrl(place.place_url)
            }
        }

        // 디바이스의 '뒤로가기' 물리버튼을 눌렀을 때 만약, 웹뷰의 돌아갈 페이지가 있다면... 엑티비티가 종료되지 않도록//
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(binding.webview.canGoBack()) binding.webview.goBack()
                else finish()
            }
        })
    }
}
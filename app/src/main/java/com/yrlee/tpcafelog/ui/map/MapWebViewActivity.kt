package com.yrlee.tpcafelog.ui.map

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ActivityMapWebViewBinding

class MapWebViewActivity : AppCompatActivity() {
    val binding by lazy { ActivityMapWebViewBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.wv.webViewClient = WebViewClient()
        binding.wv.webChromeClient = WebChromeClient()
        binding.wv.settings.javaScriptEnabled = true
        binding.wv.loadUrl("http://yrlee2025.dothome.co.kr/cafelog/cafelog_map.html")
    }
}
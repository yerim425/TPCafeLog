package com.yrlee.tpcafelog.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ActivityFullImageBinding

class FullImageActivity : AppCompatActivity() {
    val binding by lazy { ActivityFullImageBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imageUrl = intent.getStringExtra("image_url")
        Glide.with(binding.iv).load(imageUrl).into(binding.iv)

        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}
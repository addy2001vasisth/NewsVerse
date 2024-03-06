package com.example.newsapp.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivityHomeBinding
import com.example.newsapp.databinding.ActivityWebViewBinding
import com.example.newsapp.models.Article

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding
    private lateinit var webView : WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        webView = binding.webView
        setContentView(binding.root)
        val article = intent.getParcelableExtra<Article>("article")
        webView.webViewClient = WebViewClient()
        webView.loadUrl(article?.url ?: "")
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)

    }
}
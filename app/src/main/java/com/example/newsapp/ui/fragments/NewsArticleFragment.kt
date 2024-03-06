package com.example.newsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.example.newsapp.R
import com.example.newsapp.databinding.FragmentBreakingNewsFragmentsBinding
import com.example.newsapp.databinding.FragmentNewsArticleBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NewsArticleFragment : Fragment() {


    private lateinit var binding : FragmentNewsArticleBinding
    private lateinit var webView : WebView
    private val args : NewsArticleFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNewsArticleBinding.inflate(inflater,container,false)
        webView = binding.webView

        webView.webViewClient = WebViewClient()
        webView.loadUrl(args.article?.url ?: "")
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)

        return binding.root
    }
}
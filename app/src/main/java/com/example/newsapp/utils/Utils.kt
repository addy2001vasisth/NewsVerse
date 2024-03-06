package com.example.newsapp.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.newsapp.NewsApplication

object Utils {

    fun setImageUsingGlide(imageView: ImageView,url: String, context: Context = NewsApplication.appInstance!!){
        Glide.with(context).load(url).centerCrop().into(imageView)
    }
}
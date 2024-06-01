package com.example.newsapp.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.newsapp.NewsApplication

object Utils {

    fun setImageUsingGlide(imageView: ImageView,url: String, context: Context = NewsApplication.appInstance!!){
        Glide.with(context).load(url).centerCrop().into(imageView)
    }

    fun dipToPixels(context: Context, dipValue: Float): Float {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
    }
}
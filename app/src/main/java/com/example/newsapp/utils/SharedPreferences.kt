package com.example.newsapp.utils

import android.content.Context
import com.example.newsapp.NewsApplication
import com.example.newsapp.R

object SharedPreferences {

    private const val preferencesVal = "NewsVerse"
    private val sharedPref =
        NewsApplication.appInstance?.getSharedPreferences(preferencesVal,Context.MODE_PRIVATE)


    fun writeToSP(theme: String){
        sharedPref?.edit()?.putString(NewsApplication.appInstance?.getString(R.string.theme), theme)?.commit()
    }

    fun getSP(key: String) : String{
        return sharedPref?.getString(key,"") ?: ""
    }
}
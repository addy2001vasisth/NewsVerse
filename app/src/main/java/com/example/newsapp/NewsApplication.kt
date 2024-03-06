package com.example.newsapp

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NewsApplication : Application() {

    override fun onCreate() {
        appInstance = applicationContext
        super.onCreate()
    }

    companion object {
        var appInstance: Context? = null
    }


}


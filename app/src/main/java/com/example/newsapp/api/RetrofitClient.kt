package com.example.newsapp.api

import android.util.Log
import com.example.newsapp.utils.Constants.NEWS_API_KEY
import com.example.newsapp.utils.Constants.NEWS_BASE_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


object RetrofitClient {

    private var instance: NewsApi? = null

    fun getClient(): NewsApi {

        if(instance != null) return instance!!
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(Interceptor { chain ->
                val builder = chain.request().newBuilder()
                builder.header("Authorization",NEWS_API_KEY)
                return@Interceptor chain.proceed(builder.build())
            })
            .build()
        try {
            val retrofitInstance = Retrofit.Builder()
                .baseUrl(NEWS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(NewsApi::class.java)

            instance = retrofitInstance
        }catch (e:java.lang.Exception){
            Log.e("CheckingAditya",e.message.toString())
        }
        return instance!!
    }

}
package com.example.newsapp.api

import com.example.newsapp.models.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("/v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("country") countryCode: String = "in",
//        @Query("category") category: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("page") page: Int,
    ): Response<NewsResponse>

    @GET("/v2/everything")
    suspend fun getSearchNews(
        @Query("q") query: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("page") page: Int = 1,
    ): Response<NewsResponse>
}
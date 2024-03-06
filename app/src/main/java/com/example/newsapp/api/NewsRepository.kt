package com.example.newsapp.api

import androidx.lifecycle.LiveData
import com.example.newsapp.db.AppDatabase
import com.example.newsapp.models.Article

class NewsRepository(private val db : AppDatabase) {

    suspend fun getBreakingNews(page:Int) = RetrofitClient.getClient().getBreakingNews(page = page)

    suspend fun getNewsFromSearch(query: String,page: Int) = RetrofitClient.getClient().getSearchNews(query,page = page)

    suspend fun upsertArticle(article: Article) = db.userDao().upsert(article)

    fun getAllArticleFromLocalDb() : LiveData<List<Article>> = db.userDao().getAllSavedArticles()

    suspend fun deleteArticle(article: Article) = db.userDao().deleteArticle(article)

}
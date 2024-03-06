package com.example.newsapp.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.example.newsapp.models.Article

@Dao
interface NewsLsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article)

    @Query(value = "SELECT * FROM saved_articles")
    fun getAllSavedArticles() : LiveData<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}
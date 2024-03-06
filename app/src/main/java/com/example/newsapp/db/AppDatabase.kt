package com.example.newsapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.newsapp.NewsApplication
import com.example.newsapp.models.Article

@Database(entities = [Article::class], version = 1)
@TypeConverters(SourceTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): NewsLsDao
    companion object {
        private var dbInstance : AppDatabase ?= null

        fun getInstance(context: Context): AppDatabase {
            if(dbInstance != null) return dbInstance!!
            dbInstance =  Room.databaseBuilder(
                context,
                AppDatabase::class.java, "news_db"
            ).build()
            return dbInstance!!
        }
    }

}
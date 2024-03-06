package com.example.newsapp.di

import android.content.Context
import com.example.newsapp.api.NewsRepository
import com.example.newsapp.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
object FragmentModules {

    @Provides
    @FragmentScoped
    fun provideNewsRepository(db : AppDatabase) : NewsRepository {
        return NewsRepository(db)
    }

    @Provides
    @FragmentScoped
    fun provideAppDb(@ApplicationContext context : Context) : AppDatabase{
        return AppDatabase.getInstance(context)
    }
}
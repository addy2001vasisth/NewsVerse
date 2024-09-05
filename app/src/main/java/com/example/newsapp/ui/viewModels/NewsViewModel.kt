package com.example.newsapp.ui.viewModels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.newsapp.NewsApplication
import com.example.newsapp.R
import com.example.newsapp.api.NewsRepository
import com.example.newsapp.models.Article
import com.example.newsapp.models.NewsResponse
import com.example.newsapp.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.HttpException

class NewsViewModel(private val newsRepository: NewsRepository) : ViewModel() {

    val breakingNewsLiveData : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val newsFromSearchLiveData : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    fun getBreakingNews(page : Int) = viewModelScope.launch {
        breakingNewsLiveData.value = (Resource.Loading())
        try {
            val response = newsRepository.getBreakingNews(page)
            if (response.isSuccessful && response.body() != null && response.errorBody() == null) {
                breakingNewsLiveData.value = (Resource.Success(response.body()!!))
            } else if (response.errorBody() != null) {
                breakingNewsLiveData.value = (Resource.Error(null, response.body()?.status?:""))
            } else {
                Toast.makeText(NewsApplication.appInstance,
                    response.message(),
                    Toast.LENGTH_SHORT).show()
            }
        } catch(e:java.lang.Exception){
            if(e is HttpException) {
                showNoNetworkToast()
                breakingNewsLiveData.postValue(Resource.Error(null,""))
            }else {
                breakingNewsLiveData.postValue(Resource.Error(null, ""))

                Toast.makeText(NewsApplication.appInstance,
                    NewsApplication.appInstance!!.getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun getNewsFromSearch(query: String,pageNum:Int) = viewModelScope.launch {
        newsFromSearchLiveData.postValue(Resource.Loading())
        try {
            val response = newsRepository.getNewsFromSearch(query,pageNum)
            if (response.isSuccessful && response.body() != null && response.errorBody() == null) {
                newsFromSearchLiveData.postValue(Resource.Success(response.body()!!))
            } else if (response.errorBody() != null) {
                newsFromSearchLiveData.postValue(Resource.Error(null, response.message()))
            } else {
                Toast.makeText(NewsApplication.appInstance,
                    response.message(),
                    Toast.LENGTH_SHORT).show()
            }
        } catch (e:java.lang.Exception){
            if(e is HttpException) {
                newsFromSearchLiveData.postValue(Resource.Error(null,""))
                showNoNetworkToast()
            }else {
                newsFromSearchLiveData.postValue(Resource.Error(null,""))
                Toast.makeText(NewsApplication.appInstance,
                    NewsApplication.appInstance!!.getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun upsertArticleToLocalDb(article: Article) = viewModelScope.launch {
        newsRepository.upsertArticle(article)
    }

    fun deleteArticleFromLocalDb(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    fun getAllSavedArticleFromLocalDb() = newsRepository.getAllArticleFromLocalDb()


    private fun showNoNetworkToast(){
        Toast.makeText(NewsApplication.appInstance,NewsApplication.appInstance!!.getString(R.string.no_internet_connection),Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun provideFactory(
            newsRepository: NewsRepository,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory() {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return NewsViewModel(newsRepository) as T
                }
            }
    }

}
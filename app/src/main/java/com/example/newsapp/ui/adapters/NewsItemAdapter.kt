package com.example.newsapp.ui.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.databinding.LayoutLoadingBinding
import com.example.newsapp.databinding.LayoutNewsItemBinding
import com.example.newsapp.models.Article
import com.example.newsapp.ui.WebViewActivity
import com.example.newsapp.utils.Utils
import javax.inject.Inject


class NewsItemAdapter @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: LayoutNewsItemBinding
    private lateinit var bindingLoading : LayoutLoadingBinding
    private var list : MutableList<Article> = mutableListOf()
    private var activityContext: Context? = null
    private var adapterToFragCallback : AdapterToFragment ?= null
    private lateinit var loaderHolder : NewsLoadingVH

    private val diffUtil = object : DiffUtil.ItemCallback<Article>() {
        override fun areContentsTheSame(oldItem: Article, newItem: Article):
                Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
           return oldItem.url == newItem.url
        }
    }

     val asyncListDiffer = AsyncListDiffer(this, diffUtil)


    inner class NewsItemVH(itemView: LayoutNewsItemBinding) : RecyclerView.ViewHolder(itemView.root){
        val heading = itemView.heading
        val desc = itemView.description
        val dateTime = itemView.dateTime
        val img = itemView.img
        val source = itemView.source
        val archiveIc = itemView.archiveIc
    }

    inner class NewsLoadingVH(itemView: LayoutLoadingBinding): RecyclerView.ViewHolder(itemView.root){
        val loader = itemView.root
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == asyncListDiffer.currentList.size-1){
            1
        } else {
            0;
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = LayoutNewsItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        bindingLoading = LayoutLoadingBinding.inflate(LayoutInflater.from(parent.context),parent,false);

        if(viewType == 0) {
            return NewsItemVH(binding)
        } else if(viewType == 1) {
            return NewsLoadingVH(bindingLoading)
        }

        return NewsItemVH(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(position == asyncListDiffer.currentList.size-1){
            adapterToFragCallback?.lastItemReached()
        }
        val mp = holder.itemView.layoutParams as MarginLayoutParams
        mp.topMargin = 18

        when(holder.itemViewType){
            0 -> {
                holder as NewsItemVH
                val article = asyncListDiffer.currentList[position]

                holder.apply {
                    archiveIc.isVisible = article.isArchived
                    heading.text = article.title
                    desc.text = article.description
                    Utils.setImageUsingGlide(img,article.urlToImage ?:"")
                    dateTime.text = article.publishedAt
                    source.text = article.source?.name
                    itemView.setOnClickListener {
                        val intent = Intent(activityContext,WebViewActivity::class.java)
                        intent.putExtra("article",article)
                        activityContext?.startActivity(intent)
                    }
                }
            }
            1 -> {
                loaderHolder = holder as NewsLoadingVH
                val params: ViewGroup.LayoutParams = holder.itemView.layoutParams
                params.height = 130
                holder.itemView.layoutParams = params


            }

        }

    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    fun setContext(context: Context){
        this.activityContext = context
    }

    fun updateList(list: List<Article>){
        asyncListDiffer.submitList(list)
    }

//    fun addList(list:List<Article>){
//        this.list = asyncListDiffer.currentList
//        this.list.addAll(list)
//        a
//    }

    fun getArticle(pos:Int): Article{
        return asyncListDiffer.currentList[pos]
    }

    fun insertArticle(pos:Int, article: Article){
        list = asyncListDiffer.currentList.toMutableList()
        list.add(pos,article)
        updateList(list)
    }

    fun updateArticle(pos:Int, article: Article){
        list = asyncListDiffer.currentList.toMutableList()
        list[pos] = article
        updateList(list)
    }

    fun deleteArticleFromList(pos:Int) {
        list = asyncListDiffer.currentList.toMutableList()
        list.removeAt(pos)
        updateList(list)
    }

    fun settingUpAdapterToFragmentCallBack(callback: AdapterToFragment){
        adapterToFragCallback = callback
    }

    fun hideOrShowListLoader(hide: Boolean){
        loaderHolder.loader.isVisible = !hide
    }


}

interface AdapterToFragment{
    fun lastItemReached()
}
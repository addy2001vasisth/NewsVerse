package com.example.newsapp.ui.fragments

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.api.NewsRepository
import com.example.newsapp.databinding.FragmentSearchNewsBinding
import com.example.newsapp.databinding.LayoutLoadingBinding
import com.example.newsapp.models.Article
import com.example.newsapp.ui.adapters.AdapterToFragment
import com.example.newsapp.ui.adapters.NewsItemAdapter
import com.example.newsapp.ui.viewModels.NewsViewModel
import com.example.newsapp.utils.Resource
import com.example.newsapp.utils.Utils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


@AndroidEntryPoint
class SearchNewsFragment : Fragment() {

    private lateinit var binding: FragmentSearchNewsBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingLyt : LayoutLoadingBinding
    private lateinit var inputBox : EditText
    private var pageNum = 1
    private val articleMainList = mutableListOf<Article>()
    private var localDbArrList = mutableListOf<Article>()
    @Inject
    lateinit var newsRepository: NewsRepository
    @Inject
    lateinit var newsAdapter : NewsItemAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSearchNewsBinding.inflate(inflater,container,false)

        recyclerView = binding.recyclerView
        loadingLyt = binding.loadingLyt
        inputBox = binding.inputBox

        val newsViewModel: NewsViewModel by viewModels {
            NewsViewModel.provideFactory(newsRepository)
        }

        newsViewModel.getAllSavedArticleFromLocalDb().observe(viewLifecycleOwner) {
            localDbArrList = it as MutableList<Article>
            var deleteNewsItem = arrayListOf<Int>()

            for((pos,item) in articleMainList.withIndex()){
                if(item.isArchived){
                    var doFound = false
                    for(savedItem in localDbArrList){
                        if(savedItem.title.equals(item.title)){
                            doFound = true
                        }
                    }
                    if(!doFound)
                        deleteNewsItem.add(pos)
                }
            }

            for(pos in deleteNewsItem){
                newsAdapter.notifyItemChanged(pos)
            }

            for(article in articleMainList){
                article.isArchived = false
                for(savedArticle in localDbArrList){
                    if(savedArticle.title.equals(article.title)){
                        article.isArchived = true
                    }
                }
            }


            newsAdapter.updateList(articleMainList)
        }

        recyclerView.adapter = newsAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)




        newsViewModel.newsFromSearchLiveData.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    loadingLyt.root.visibility = View.GONE
                    val articleList = it.data!!.articles
                    newsRepository.getAllArticleFromLocalDb().observe(viewLifecycleOwner){
                        for(article in articleList){
                            if(!article.isArchived) {
                                if (it != null) {
                                    for (saved in it) {
                                        if (article.title == saved.title) {
                                            article.isArchived = true
                                        }
                                    }
                                }
                            }
                        }
                        articleMainList.addAll(articleList)
                        newsAdapter.notifyDataSetChanged()
                        newsAdapter.updateList(articleMainList)


                    }

                    newsAdapter.setContext(context!!)

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(2000)
                        if(it.data!!.articles.isEmpty()){
                            newsAdapter.hideOrShowListLoader(true)

                        }
                    }
                }
                is Resource.Error -> {
                    loadingLyt.root.visibility = View.GONE
                }
                is Resource.Loading -> {
                    if(pageNum < 2)
                        loadingLyt.root.visibility = View.VISIBLE
                }
            }
        }

        newsAdapter.settingUpAdapterToFragmentCallBack(object: AdapterToFragment {
            override fun lastItemReached() {
                newsViewModel.getNewsFromSearch(inputBox.text.toString(),++pageNum)
            }

        })

        var job : Job ?= null
        inputBox.addTextChangedListener{
            pageNum = 1
            articleMainList.clear()
            job?.cancel()
            job = CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                if(inputBox.text.toString().isNotEmpty()) {
                    newsViewModel.getNewsFromSearch(inputBox.text.toString(), pageNum)
                }
            }
        }


        val swipeToDeleteCallBack = object : ItemTouchHelper.Callback() {
            private val deleteDrawableIc = ContextCompat.getDrawable(activity!!,
                R.drawable.ic_archive)

            var bitmap = (deleteDrawableIc)?.toBitmap()

            var deleteDrawable: Drawable =
                BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap!!, 100, 100, true))

            private val intrinsicWidth = deleteDrawable.intrinsicWidth
            private val intrinsicHeight = deleteDrawable.intrinsicHeight

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ): Int {
                return makeMovementFlags(0, ItemTouchHelper.LEFT)
            }


            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }


            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                var article = newsAdapter.getArticle(pos)
                if (article.isArchived) {
                    Snackbar.make(view!!, getString(R.string.item_is_already_saved), Snackbar.LENGTH_SHORT).show()
                    newsAdapter.notifyItemChanged(pos)
                    return
                }

                article.isArchived = true
                newsViewModel.upsertArticleToLocalDb(article)

                articleMainList[pos] = article

                newsAdapter.updateList(articleMainList)
                newsAdapter.notifyItemChanged(pos)


                Snackbar.make(view!!, getString(R.string.selected_item_has_been_saved), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.undo)){
                        val article = localDbArrList[ localDbArrList.size-1]
                        article.isArchived = false
                        newsViewModel.deleteArticleFromLocalDb(article)
                        Snackbar.make(view!!,getString(R.string.removed_from_saved_articles),Snackbar.LENGTH_SHORT).show()
                        articleMainList[pos] = article
                        newsAdapter.updateList(articleMainList)
                        newsAdapter.notifyItemChanged(pos)


                    }
                    .show()



            }


            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean,
            ) {
                super.onChildDraw(c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive)
                val itemView = viewHolder.itemView
                val itemHeight = itemView.height

                val paint = Paint()
                paint.color = context!!.getColor(R.color.teal_700)
                c.drawRoundRect(RectF(itemView.right+dX-100,itemView.top.toFloat(),itemView.right.toFloat(),itemView.bottom.toFloat()),
                    Utils.dipToPixels(activity!!,10f),
                    Utils.dipToPixels(activity!!,10f),paint)

                deleteDrawable.setTint(resources.getColor(R.color.white))

                val deleteIconTop: Int = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin: Int = (itemHeight - intrinsicHeight) / 2
                val deleteIconBottom: Int = deleteIconTop + intrinsicHeight


                deleteDrawable.setBounds(
                    itemView.right - (intrinsicWidth*3/2) + (dX/5).toInt(),
                    deleteIconTop,
                    itemView.right - (intrinsicWidth/2) + (dX/5).toInt(),
                    deleteIconBottom)

                deleteDrawable.draw(c)


                super.onChildDraw(c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive)

            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return binding.root
    }




}
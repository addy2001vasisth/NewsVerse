package com.example.newsapp.ui.fragments

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.api.NewsRepository
import com.example.newsapp.databinding.FragmentBreakingNewsFragmentsBinding
import com.example.newsapp.databinding.LayoutLoadingBinding
import com.example.newsapp.models.Article
import com.example.newsapp.models.NewsResponse
import com.example.newsapp.ui.adapters.AdapterToFragment
import com.example.newsapp.ui.adapters.NewsItemAdapter
import com.example.newsapp.ui.viewModels.NewsViewModel
import com.example.newsapp.utils.Resource
import com.example.newsapp.utils.Utils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BreakingNewsFragment : Fragment() {

    private lateinit var binding: FragmentBreakingNewsFragmentsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingLyt: LayoutLoadingBinding
    private var pageNum = 1
    private var listOfArticles = mutableListOf<Article>()
    private var localDbArrList = mutableListOf<Article>()

    @Inject
    lateinit var newsRepository: NewsRepository

    @Inject
    lateinit var newsAdapter: NewsItemAdapter

    private val pageNumVsArticlesMap  = hashMapOf<Int,List<Article>>()

    val newsViewModel: NewsViewModel by viewModels {
        NewsViewModel.provideFactory(newsRepository)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        newsViewModel.getBreakingNews(pageNum)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBreakingNewsFragmentsBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerView
        loadingLyt = binding.loadingLyt
        recyclerView.adapter = newsAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)


        newsViewModel.getAllSavedArticleFromLocalDb().observe(viewLifecycleOwner) {
            localDbArrList = it as MutableList<Article>
            var deleteNewsItem = arrayListOf<Int>()

            for((pos,item) in listOfArticles.withIndex()){
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

            for(article in listOfArticles){
                article.isArchived = false
                for(savedArticle in localDbArrList){
                        if(savedArticle.title.equals(article.title)){
                            article.isArchived = true
                        }
                }
            }


            newsAdapter.updateList(listOfArticles)
        }





        newsViewModel.breakingNewsLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {

                    loadingLyt.root.visibility = View.GONE
                    var articleList = it.data!!.articles
                    this.listOfArticles.clear()

                    for(article in articleList){
                        for(savedArticle in localDbArrList){
                            if(!article.isArchived){
                                if(savedArticle.title.equals(article.title)){
                                    article.isArchived = true
                                }
                            }
                        }
                    }

                    pageNumVsArticlesMap[pageNum] = articleList


                    for((key,value) in pageNumVsArticlesMap){
                        listOfArticles.addAll(value)
                    }
                    if(articleList.isEmpty()){
                        newsAdapter.hideOrShowListLoader(true)
                        pageNum--

                    }





                    var oldsize = listOfArticles.size
                    if(listOfArticles.size != 0){
                        listOfArticles.add(Article(0,null,null,null,null,null,null,null,null))
                    }

                    newsAdapter.updateList(listOfArticles)
                    newsAdapter.notifyItemRangeInserted(oldsize,articleList.size)
                    newsAdapter.setContext(context!!)


                }
                is Resource.Error -> {
                    loadingLyt.root.visibility = View.GONE
                    Toast.makeText(activity!!, it.message.toString(), Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    if (pageNum < 2) {
                        loadingLyt.root.visibility = View.VISIBLE
                    }
                }
            }
        }


        newsAdapter.settingUpAdapterToFragmentCallBack(object : AdapterToFragment {
            override fun lastItemReached() {
                newsViewModel.getBreakingNews(++pageNum)
            }

        })

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

                listOfArticles[pos] = article

                newsAdapter.updateList(listOfArticles)
                newsAdapter.notifyItemChanged(pos)


                Snackbar.make(view!!, getString(R.string.selected_item_has_been_saved), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.undo)){
                        val article = localDbArrList[ localDbArrList.size-1]
                        article.isArchived = false
                        newsViewModel.deleteArticleFromLocalDb(article)
                        Snackbar.make(view!!,getString(R.string.removed_from_saved_articles),Snackbar.LENGTH_SHORT).show()
                        listOfArticles[pos] = article
                        newsAdapter.updateList(listOfArticles)
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
                c.drawRoundRect(RectF(itemView.right+dX-100,itemView.top.toFloat(),itemView.right.toFloat(),itemView.bottom.toFloat()),Utils.dipToPixels(activity!!,10f),Utils.dipToPixels(activity!!,10f),paint)

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
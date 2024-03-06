package com.example.newsapp.ui.fragments

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
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

        recyclerView.adapter = newsAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val newsViewModel: NewsViewModel by viewModels {
            NewsViewModel.provideFactory(newsRepository)
        }


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
            private val mClearPaint = Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
            private val mBackground: ColorDrawable = ColorDrawable()
            private val backgroundColor = activity?.getColor(R.color.teal_700)
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
                if(article.isArchived){
                    Snackbar.make(view!!,"Item is already archived", Snackbar.LENGTH_SHORT).show()
                    newsAdapter.deleteArticleFromList(pos)
                    Handler().postDelayed({
                        newsAdapter.setArticle(pos,article)
                    },500)
                    return
                }
                newsViewModel.upsertArticleToLocalDb(article)
                article.isArchived = true
                newsAdapter.deleteArticleFromList(pos)
                Snackbar.make(view!!,"Selected Item has been archived..", Snackbar.LENGTH_SHORT).show()


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
                val isCancelled = dX == 0f && !isCurrentlyActive
                if (isCancelled) {

                    c.drawRect(itemView.right + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat(),
                        mClearPaint)

                    super.onChildDraw(c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive)
                    return
                }
                mBackground.color = backgroundColor!!
                mBackground.setBounds(itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom)
                mBackground.draw(c)
                deleteDrawable.setTint(resources.getColor(R.color.white))

                val deleteIconTop: Int = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin: Int = (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft: Int = itemView.right - deleteIconMargin - intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom: Int = deleteIconTop + intrinsicHeight
                deleteDrawable.setBounds(deleteIconLeft,
                    deleteIconTop,
                    deleteIconRight,
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

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            inputBox.requestFocus()
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.showSoftInput(inputBox, InputMethodManager.SHOW_IMPLICIT)
        },500)

    }



}
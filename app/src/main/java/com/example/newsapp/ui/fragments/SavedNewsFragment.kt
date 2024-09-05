package com.example.newsapp.ui.fragments

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.api.NewsRepository
import com.example.newsapp.databinding.FragmentSavedNewsBinding
import com.example.newsapp.databinding.LayoutLoadingBinding
import com.example.newsapp.models.Article
import com.example.newsapp.ui.adapters.NewsItemAdapter
import com.example.newsapp.ui.viewModels.NewsViewModel
import com.example.newsapp.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SavedNewsFragment : Fragment() {

    private lateinit var binding : FragmentSavedNewsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingLyt : LayoutLoadingBinding

    @Inject
    lateinit var newsRepository: NewsRepository
    @Inject
    lateinit var newsAdapter : NewsItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSavedNewsBinding.inflate(inflater,container,false)

        recyclerView = binding.recyclerView
        loadingLyt = binding.loadingLyt

        recyclerView.adapter = newsAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val newsViewModel: NewsViewModel by viewModels {
            NewsViewModel.provideFactory(newsRepository)
        }
        newsViewModel.getAllSavedArticleFromLocalDb().observe(viewLifecycleOwner){
            it?.let {



                var list = it.toMutableList()
                list.add(Article(0,null,null,null,null,null,null,null,null))
                newsAdapter.updateList(list)
                newsAdapter.setContext(context!!)

                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                        newsAdapter.hideOrShowListLoader(true)

                }

            }

        }

        val swipeToDeleteCallBack = object : ItemTouchHelper.Callback() {
            private val mClearPaint = Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
            private val mBackground: ColorDrawable = ColorDrawable()
            private val backgroundColor = activity!!.getColor(R.color.red)
            private val deleteDrawableIc = ContextCompat.getDrawable(activity!!,
                R.drawable.ic_delete)

            var bitmap = (deleteDrawableIc)?.toBitmap()

            var deleteDrawable: Drawable =
                BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap!!, 100, 100, true))

            private val intrinsicWidth = deleteDrawable.intrinsicWidth!!
            private val intrinsicHeight = deleteDrawable.intrinsicHeight!!


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
                newsViewModel.deleteArticleFromLocalDb(article)

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
                paint.color = context!!.getColor(R.color.red)
                c.drawRoundRect(RectF(itemView.right+dX-100,itemView.top.toFloat(),itemView.right.toFloat(),itemView.bottom.toFloat()),
                    Utils.dipToPixels(activity!!,10f),
                    Utils.dipToPixels(activity!!,10f),paint)

                deleteDrawable.setTint(resources.getColor(R.color.white))

                val deleteIconTop: Int = itemView.top + (itemHeight - intrinsicHeight!!) / 2
                val deleteIconMargin: Int = (itemHeight - intrinsicHeight) / 2
                val deleteIconBottom: Int = deleteIconTop + intrinsicHeight


                deleteDrawable.setBounds(
                    itemView.right - ((intrinsicWidth.times(3)) /2) + (dX/5).toInt(),
                    deleteIconTop,
                    itemView.right - (intrinsicWidth.div(2)) + (dX/5).toInt(),
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
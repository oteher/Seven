package com.luuu.seven.module.rank

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luuu.seven.R
import com.luuu.seven.adapter.ComicRankAdapter
import com.luuu.seven.base.BaseFragment
import com.luuu.seven.bean.ComicUpdateBean
import com.luuu.seven.bean.HotComicBean
import com.luuu.seven.module.index.HomeViewModel
import com.luuu.seven.module.intro.ComicIntroActivity
import com.luuu.seven.util.*
import com.luuu.seven.widgets.RankItemDecoration
import kotlinx.android.synthetic.main.fra_tab_layout.*

/**
 * Created by lls on 2017/8/4.
 * 排行二级fragment
 */
class ComicRankInnerFragment : BaseFragment() {

    private lateinit var mViewModel: HomeViewModel
    private var mRankBeanList: ArrayList<HotComicBean> = ArrayList()
    private var mHotComicBeanList: MutableList<HotComicBean>? = null
    private var mHotComicTopList: MutableList<HotComicBean>? = null
    private var mPageNum = 0
    private var pos = 0
    private val num by lazy {
        arguments?.getInt(COMIC_TYPE)
    }
    private var mAdapter: ComicRankAdapter? = null
    private val mLayoutManager by lazy { LinearLayoutManager(mContext) }

    companion object {
        const val COMIC_TYPE = "type"

        fun newInstance(type: Int): ComicRankInnerFragment {
            val fragment = ComicRankInnerFragment()
            val bundle = Bundle()
            bundle.putInt(COMIC_TYPE, type)
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun onResume() {
        super.onResume()
        mHotComicBeanList = ArrayList()
        mHotComicTopList = ArrayList()
        mPageNum = 0
        mViewModel.getRankComic(num ?: 0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHotComicTopList = null
        mHotComicBeanList = null
    }

    override fun initViews() {
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                refresh.isEnabled = mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0
            }
        })
        refresh.setOnRefreshListener {
            mPageNum = 0
            mViewModel.getRankComic(num ?: 0, mPageNum)
        }

        mViewModel = obtainViewModel<HomeViewModel>().apply {

            rankData.observe(viewLifecycleOwner, Observer { data ->
                mRankBeanList.addAll(data)

                mAdapter?.let { adapter ->
                    adapter.loadMoreComplete()

                    if (refresh.isRefreshing) {
                        refresh.isRefreshing = false
                    } else {
                        if (data.isEmpty()) {
                            adapter.loadMoreEnd()
                        } else {
                            adapter.notifyDataSetChanged()
                        }
                    }
                } ?: initAdapter()
            })
        }
    }

    override fun getContentViewLayoutID(): Int = R.layout.fra_tab_layout

    private fun initAdapter() {
        mAdapter = ComicRankAdapter(R.layout.item_rank_layout, mRankBeanList).apply {
            setEnableLoadMore(true)
            setOnLoadMoreListener({
                mPageNum++
                mViewModel.getRankComic(num ?: 0, mPageNum)
            }, recycler)
            setOnItemClickListener { _, _, position ->
                val mBundle = Bundle()
                mBundle.putInt("comicId", mRankBeanList[position].comicId)
                startNewActivity(ComicIntroActivity::class.java, mBundle)
            }
        }

        recycler.layoutManager = mLayoutManager
        recycler.addItemDecoration(RankItemDecoration(mContext!!))
        recycler.adapter = mAdapter


    }
}
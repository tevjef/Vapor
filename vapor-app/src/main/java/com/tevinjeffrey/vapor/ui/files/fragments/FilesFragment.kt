package com.tevinjeffrey.vapor.ui.files.fragments


import android.content.res.Resources
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar

import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.tevinjeffrey.vapor.R
import com.tevinjeffrey.vapor.VaporApp
import com.tevinjeffrey.vapor.customviews.MarginDecoration
import com.tevinjeffrey.vapor.events.DeleteEvent
import com.tevinjeffrey.vapor.events.RenameEvent
import com.tevinjeffrey.vapor.events.UploadEvent
import com.tevinjeffrey.vapor.okcloudapp.DataManager
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType
import com.tevinjeffrey.vapor.ui.base.Presenter
import com.tevinjeffrey.vapor.ui.base.View
import com.tevinjeffrey.vapor.ui.files.FilesActivityPresenter
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.ArchivePresenter
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.AudioPresenter
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BookmarkPresenter
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.FilesPresenter
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.ImagePresenter
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.RecentPresenter
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.TextPresenter
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.UnknownPresenter
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.VideoPresenter
import com.tevinjeffrey.vapor.ui.login.LoginException
import com.tevinjeffrey.vapor.ui.utils.EndlessRecyclerOnScrollListener

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.ArrayList
import java.util.Collections

import javax.inject.Inject

import butterknife.Bind
import butterknife.ButterKnife
import icepick.Icicle
import timber.log.Timber

import android.view.View.GONE
import android.view.View.VISIBLE

class FilesFragment : MVPFragment(), FilesFragmentView, SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var bus: Bus

    @Icicle
    internal var mViewState = FilesFragmentViewState()

    @Icicle
    var dataCursor: DataManager.DataCursor? = null

    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var files_recyclerview: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var empty_view: ViewGroup

    override val cursor: DataManager.DataCursor
        get() = dataCursor!!

    private var mItemType: ItemType? = null

    private var mListDataSet: MutableList<CloudAppItem>? = null

    override var isVisibleInPager: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)
        VaporApp.uiComponent(parentActivity).inject(this)
        if (arguments != null) {
            mItemType = arguments.getSerializable(ITEM_TYPE) as ItemType?
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): android.view.View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_files_list, container, false)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        files_recyclerview = view.findViewById(R.id.files_recyclerview) as RecyclerView
        empty_view = view.findViewById(R.id.empty_view) as ViewGroup
        progressBar = view.findViewById(R.id.progressBar) as ProgressBar

        return view
    }

    override fun onViewCreated(view: android.view.View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (presenter == null) {
            setPresenter(typedPresenter)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle) {
        super.onActivityCreated(savedInstanceState)
        //Attach view to presenter
        mViewState.apply(this, savedInstanceState != null)

        if (dataCursor == null) {
            dataCursor = DataManager.DataCursor(presenter.javaClass.simpleName)
        }
        presenter.attachView(this)
        if (presenter.isNotLoading) {
            presenter.loadData(true, false, true)
        }
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)

    }

    override fun onPause() {
        super.onPause()
        bus.unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private val presenter: FilesPresenter<FilesFragmentView>
        get() = mPresenter!!

    fun setPresenter(filesPresenter: FilesPresenter<FilesFragmentView>) {
        mPresenter = filesPresenter
    }

    override fun showLoading(pullToRefresh: Boolean) {
        mViewState.isRefreshing = pullToRefresh
        swipeRefreshLayout!!.post {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout!!.isRefreshing = pullToRefresh
            }
        }
    }

    override fun setData(data: List<CloudAppItem>) {
        if (mListDataSet == data && mListDataSet!!.size != 0) {
            return
        }
        if (mListDataSet == null) {
            return
        }
        val itemIterator = data.iterator()
        while (itemIterator.hasNext()) {
            var currentItem = itemIterator.next()
            for (i in mListDataSet!!.indices) {
                if (mListDataSet!![i] == currentItem) {
                    // Only updated items that have changed
                    if (mListDataSet!![i].hash() != currentItem.hash()) {
                        mListDataSet!![i] = currentItem
                        files_recyclerview.adapter.notifyItemChanged(i)
                    }
                    break
                }
            }
        }
        
        appendData(data.filter { mListDataSet!!.contains(it) })
    }

    override fun appendData(data: List<CloudAppItem>) {
        mListDataSet!!.addAll(data)
        if (mListDataSet!!.size == 0 && presenter.shouldShowEmpty()) {
            showLayout(View.LayoutType.EMPTY)
        }

        if (mListDataSet!!.size > 0) {
            showLayout(View.LayoutType.LIST)
        }

        mViewState.data = mListDataSet!!
        if (parentActivity.presenter!!.navContext !== FilesActivityPresenter.NavContext.POPULAR &&
                parentActivity.presenter!!.navContext !== FilesActivityPresenter.NavContext.TRASH) {
            if (!Companion.isSorted(mListDataSet!!, false)) {
                Collections.sort(mListDataSet)
                files_recyclerview.adapter.notifyDataSetChanged()
            }
        }
        if (data.size != 0) {
            files_recyclerview.adapter.notifyItemRangeInserted(mListDataSet!!.size, data.size)
        }
    }

    override fun showError(t: Throwable) {
        val message: String
        val resources = resources

        if (t is UnknownHostException) {
            message = resources.getString(R.string.no_internet)
        } else if (t is SocketTimeoutException) {
            message = resources.getString(R.string.timed_out)
        } else if (t is LoginException) {
            message = resources.getString(R.string.user_not_logged_in)
            showLayout(View.LayoutType.LOADING)
        } else {
            Timber.e(t, "show error")
            message = t.message!!
        }
        mViewState.errorMessage = message
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction("Retry") { showLoading(true) }.show()
    }

    override fun initRecyclerView() {
        val layoutManager = files_recyclerview.layoutManager as GridLayoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        files_recyclerview.layoutManager = layoutManager
        files_recyclerview.setHasFixedSize(true)
        files_recyclerview.addItemDecoration(MarginDecoration(parentActivity))
        if (mListDataSet == null) {
            mListDataSet = ArrayList<CloudAppItem>(100)
        }

        if (files_recyclerview.adapter == null) {
            /*   AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter();
            animationAdapter.setFirstOnly(false);
            animationAdapter.setDuration(50);*/
            files_recyclerview.adapter = FilesFragmentAdapter(mListDataSet!!, parentActivity)
        }

        files_recyclerview.addOnScrollListener(object : EndlessRecyclerOnScrollListener(layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                if (presenter.isNotLoading) {
                    presenter.loadData(false, false, true)
                }
            }
        })
    }

    override fun initSwipeLayout() {
        swipeRefreshLayout!!.setOnRefreshListener(this)
        swipeRefreshLayout!!.setSize(SwipeRefreshLayout.DEFAULT)
        swipeRefreshLayout!!.setColorSchemeResources(R.color.primary, R.color.accent)
    }

    override fun showLayout(type: View.LayoutType) {
        mViewState.layoutType = type
        when (type) {
            View.LayoutType.EMPTY -> {
                showRecyclerView(GONE)
                showLoadingView(GONE)
                showEmptyLayout(VISIBLE)
            }
            View.LayoutType.LIST -> {
                showEmptyLayout(GONE)
                showLoadingView(GONE)
                showRecyclerView(VISIBLE)
            }
            View.LayoutType.LOADING -> {
                showEmptyLayout(GONE)
                showRecyclerView(GONE)
                showLoadingView(VISIBLE)
            }
            else -> throw RuntimeException("Unknown type: " + type)
        }
    }

    private fun showEmptyLayout(visibility: Int) {
        if (empty_view.visibility != visibility)
            empty_view.visibility = visibility
    }

    private fun showRecyclerView(visibility: Int) {
        if (files_recyclerview.visibility != visibility)
            files_recyclerview.visibility = visibility
    }

    private fun showLoadingView(visibility: Int) {
        if (progressBar.visibility != visibility)
            progressBar.visibility = visibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ButterKnife.unbind(this)
    }

    private val typedPresenter: FilesPresenter<FilesFragmentView>
        get() {
            val presenter: FilesPresenter<FilesFragmentView>
            when (mItemType) {
                CloudAppItem.ItemType.ALL -> {
                    var recentPresenter = VaporApp.uiComponent(parentActivity).recentPresenter
                    VaporApp.uiComponent(parentActivity).inject(recentPresenter)
                    presenter = recentPresenter
                }
                CloudAppItem.ItemType.IMAGE -> {
                    val imagePresenter = VaporApp.uiComponent(parentActivity).imagePresenter
                    VaporApp.uiComponent(parentActivity).inject(imagePresenter)
                    presenter = imagePresenter
                }
                CloudAppItem.ItemType.VIDEO -> {
                    val videoPresenter = VaporApp.uiComponent(parentActivity).videoPresenter
                    VaporApp.uiComponent(parentActivity).inject(videoPresenter)
                    presenter = videoPresenter
                }
                CloudAppItem.ItemType.ARCHIVE -> {
                    val archivePresenter = VaporApp.uiComponent(parentActivity).archivePresenter
                    VaporApp.uiComponent(parentActivity).inject(archivePresenter)
                    presenter = archivePresenter
                }
                CloudAppItem.ItemType.TEXT -> {
                    val textPresenter = VaporApp.uiComponent(parentActivity).textPresenter
                    VaporApp.uiComponent(parentActivity).inject(textPresenter)
                    presenter = textPresenter
                }
                CloudAppItem.ItemType.AUDIO -> {
                    val audioPresenter = VaporApp.uiComponent(parentActivity).audioPresenter
                    VaporApp.uiComponent(parentActivity).inject(audioPresenter)
                    presenter = audioPresenter
                }
                CloudAppItem.ItemType.BOOKMARK -> {
                    val bookmarkPresenter = VaporApp.uiComponent(parentActivity).bookmarkPresenter
                    VaporApp.uiComponent(parentActivity).inject(bookmarkPresenter)
                    presenter = bookmarkPresenter
                }
                CloudAppItem.ItemType.UNKNOWN -> {
                    val unknownPresenter = VaporApp.uiComponent(parentActivity).unknownPresenter
                    VaporApp.uiComponent(parentActivity).inject(unknownPresenter)
                    presenter = unknownPresenter
                }
                else -> {
                    var recentPresenter = VaporApp.uiComponent(parentActivity).recentPresenter
                    VaporApp.uiComponent(parentActivity).inject(recentPresenter)
                    presenter = recentPresenter
                }
            }
            return presenter
        }

    override fun toString(): String {
        return "FilesFragmentFragment{" +
                "presenter=" + presenter.javaClass.simpleName +
                '}'
    }

    override fun onRefresh() {
        if (presenter.isNotLoading) {
            presenter.loadData(true, true, false)
        }
    }

    @Subscribe
    fun onRenameEvent(event: RenameEvent) {
        if (mListDataSet != null) {
            for (i in mListDataSet!!.indices) {
                if (mListDataSet!![i].itemId == event.item.itemId) {
                    mListDataSet!![i] = event.item
                    files_recyclerview.adapter.notifyItemChanged(i)
                }
            }
        }
    }

    @Subscribe
    fun onDeleteEvent(event: DeleteEvent) {
        if (mListDataSet != null) {
            for (i in mListDataSet!!.indices) {
                if (mListDataSet!![i].itemId == event.item.itemId && event.item.deletedAt != null) {
                    mListDataSet!!.removeAt(i)
                    files_recyclerview.adapter.notifyItemRemoved(i)
                }
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isVisibleInPager = view != null && isVisibleToUser
    }

    @Subscribe
    fun onUploadEvent(event: UploadEvent) {
        if (mListDataSet != null) {
            mListDataSet!!.add(0, event.item)
            files_recyclerview.adapter.notifyItemInserted(0)
        }
    }

    companion object {
        private val ITEM_TYPE = "ITEM_TYPE"

        fun newInstance(itemType: ItemType): FilesFragment {
            val fragment = FilesFragment()
            val bundle = Bundle()
            bundle.putSerializable(ITEM_TYPE, itemType)
            fragment.arguments = bundle
            return fragment
        }

        fun <E : Comparable<E>> isSorted(list: List<E>, asc: Boolean): Boolean {
            if (list.size == 0) return true
            for (i in list.indices) {
                if (i != list.size - 1) {
                    val comp = list[i].compareTo(list[i + 1])
                    val to = if (asc) -1 else 1
                    if (comp != to && comp != 0) {
                        return false
                    }
                }
            }
            return true
        }
    }
}// Required empty public constructor

package com.tevinjeffrey.vapor.ui.files.fragments;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.VaporApp;
import com.tevinjeffrey.vapor.events.DeleteEvent;
import com.tevinjeffrey.vapor.events.RenameEvent;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType;
import com.tevinjeffrey.vapor.ui.base.Presenter;
import com.tevinjeffrey.vapor.ui.base.View;
import com.tevinjeffrey.vapor.ui.files.FilesActivityPresenter;
import com.tevinjeffrey.vapor.ui.files.FilesFragmentAdapter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.ArchivePresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.AudioPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BookmarkPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.FilesPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.ImagePresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.RecentPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.TextPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.UnknownPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.VideoPresenter;
import com.tevinjeffrey.vapor.ui.login.LoginException;
import com.tevinjeffrey.vapor.ui.utils.EndlessRecyclerOnScrollListener;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icicle;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FilesFragment extends MVPFragment implements FilesView, SwipeRefreshLayout.OnRefreshListener {
    private static final String ITEM_TYPE = "ITEM_TYPE";
    @Bind(R.id.files_recyclerview) RecyclerView mRecyclerView;
    @Bind(R.id.empty_view) LinearLayout mEmptyView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.swipeRefreshLayout) SwipeRefreshLayout mSwipeRefreshLayout;

    @Inject Bus bus;

    @Icicle FilesViewState mViewState = new FilesViewState();

    @Icicle
    DataManager.DataCursor cursor;

    private ItemType mItemType;

    private List<CloudAppItem> mListDataSet;

    public static FilesFragment newInstance(ItemType itemType) {
        FilesFragment fragment = new FilesFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ITEM_TYPE, itemType);
        fragment.setArguments(bundle);
        return fragment;
    }

    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        VaporApp.uiComponent(getParentActivity()).inject(this);
        if (getArguments() != null) {
            mItemType = (ItemType) getArguments().getSerializable(ITEM_TYPE);
        }
    }

    @Override
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container,
                                          Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        android.view.View view = inflater.inflate(R.layout.fragment_files_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPresenter == null) {
            setPresenter(getTypedPresenter());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Attach view to presenter
        mViewState.apply(this, savedInstanceState != null);

        if (getCursor() == null) {
            cursor = new DataManager.DataCursor(getPresenter().getClass().getSimpleName());
        }
        mPresenter.attachView(this);
        if (!getPresenter().isLoading()) {
            getPresenter().loadData(true, false, true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    private FilesPresenter getPresenter() {
        return (FilesPresenter) mPresenter;
    }

    public void setPresenter(Presenter filesPresenter) {
        mPresenter = filesPresenter;
    }

    @Override
    public void showLoading(final boolean pullToRefresh) {
        mViewState.isRefreshing = pullToRefresh;
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(pullToRefresh);
                }
            }
        });
    }

    @Override
    public void setData(List<CloudAppItem> data) {
        if(mListDataSet.equals(data) && mListDataSet.size() != 0){
           return;
        }
        Iterator<CloudAppItem> itemIterator = data.iterator();
        while (itemIterator.hasNext()) {
            CloudAppItem currentItem = itemIterator.next();
            for (int i = 0; i < mListDataSet.size(); i++) {
                if (mListDataSet.get(i).equals(currentItem)) {
                    mListDataSet.remove(i);
                    mListDataSet.add(i, currentItem);
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    itemIterator.remove();
                    break;
                }
            }
        }
        appendData(data);
    }

    public void appendData(List<CloudAppItem> data) {
        mListDataSet.addAll(data);
        if (mListDataSet.size() == 0)
            showLayout(View.LayoutType.EMPTY);

        if (mListDataSet.size() > 0)
            showLayout(View.LayoutType.LIST);

        mViewState.data = mListDataSet;
        if (getParentActivity().getPresenter().getNavContext() != FilesActivityPresenter.NavContext.POPULAR ||
                getParentActivity().getPresenter().getNavContext() != FilesActivityPresenter.NavContext.TRASH) {
            Collections.sort(mListDataSet);
        }
        if (data.size() != 0) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void showError(Throwable t) {
        String message;
        Resources resources = getResources();

        if (t instanceof UnknownHostException) {
            message = resources.getString(R.string.no_internet);
        } else if (t instanceof SocketTimeoutException) {
            message = resources.getString(R.string.timed_out);
        } else if (t instanceof LoginException) {
            message = resources.getString(R.string.user_not_logged_in);
            showLayout(LayoutType.LOADING);
        } else {
            Timber.e(t, "show error");
            message = t.getMessage();
        }
        mViewState.errorMessage = message;
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    public void initRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getParentActivity(), 2);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        if (mListDataSet == null) {
            mListDataSet = new ArrayList<>(100);
        }

        if (mRecyclerView.getAdapter() == null) {
         /*   AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter();
            animationAdapter.setFirstOnly(false);
            animationAdapter.setDuration(50);*/
            mRecyclerView.setAdapter(new FilesFragmentAdapter(mListDataSet, getParentActivity()));
        }

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                getPresenter().loadData(false, false, true);
            }
        });
    }

    public void initSwipeLayout() {
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);
    }

    @Override
    public void showLayout(LayoutType type) {
        mViewState.layoutType = type;
        switch (type) {
            case EMPTY:
                showRecyclerView(GONE);
                showLoadingView(GONE);
                showEmptyLayout(VISIBLE);
                break;
            case LIST:
                showEmptyLayout(GONE);
                showLoadingView(GONE);
                showRecyclerView(VISIBLE);
                break;
            case LOADING:
                showEmptyLayout(GONE);
                showRecyclerView(GONE);
                showLoadingView(VISIBLE);
                break;
            default:
                throw new RuntimeException("Unknown type: " + type);
        }
    }

    @Override
    public DataManager.DataCursor getCursor() {
        return cursor;
    }

    private void showEmptyLayout(int visibility) {
        if (mEmptyView.getVisibility() != visibility)
            mEmptyView.setVisibility(visibility);
    }

    private void showRecyclerView(int visibility) {
        if (mRecyclerView.getVisibility() != visibility)
            mRecyclerView.setVisibility(visibility);
    }

    private void showLoadingView(int visibility) {
        if (mProgressBar.getVisibility() != visibility)
            mProgressBar.setVisibility(visibility);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private Presenter getTypedPresenter() {
        Presenter presenter;
        switch (mItemType) {
            case ALL:
                RecentPresenter recentPresenter = VaporApp.uiComponent(getParentActivity()).getRecentPresenter();
                VaporApp.uiComponent(getParentActivity()).inject(recentPresenter);
                presenter = recentPresenter;
                break;
            case IMAGE:
                ImagePresenter imagePresenter = VaporApp.uiComponent(getParentActivity()).getImagePresenter();
                VaporApp.uiComponent(getParentActivity()).inject(imagePresenter);
                presenter = imagePresenter;
                break;
            case VIDEO:
                VideoPresenter videoPresenter = VaporApp.uiComponent(getParentActivity()).getVideoPresenter();
                VaporApp.uiComponent(getParentActivity()).inject(videoPresenter);
                presenter = videoPresenter;
                break;
            case ARCHIVE:
                ArchivePresenter archivePresenter = VaporApp.uiComponent(getParentActivity()).getArchivePresenter();
                VaporApp.uiComponent(getParentActivity()).inject(archivePresenter);
                presenter = archivePresenter;                
                break;
            case TEXT:
                TextPresenter textPresenter = VaporApp.uiComponent(getParentActivity()).getTextPresenter();
                VaporApp.uiComponent(getParentActivity()).inject(textPresenter);
                presenter = textPresenter;
                break;
            case AUDIO:
                AudioPresenter audioPresenter = VaporApp.uiComponent(getParentActivity()).getAudioPresenter();
                VaporApp.uiComponent(getParentActivity()).inject(audioPresenter);
                presenter = audioPresenter;
                break;
            case BOOKMARK:
                BookmarkPresenter bookmarkPresenter = VaporApp.uiComponent(getParentActivity()).getBookmarkPresenter();
                VaporApp.uiComponent(getParentActivity()).inject(bookmarkPresenter);
                presenter = bookmarkPresenter;
                break;
            case UNKNOWN:
                UnknownPresenter unknownPresenter = VaporApp.uiComponent(getParentActivity()).getUnknownPresenter();
                VaporApp.uiComponent(getParentActivity()).inject(unknownPresenter);
                presenter = unknownPresenter;
                break;
            default:
                recentPresenter = VaporApp.uiComponent(getParentActivity()).getRecentPresenter();
                VaporApp.uiComponent(getParentActivity()).inject(recentPresenter);
                presenter = recentPresenter;
        }
        return presenter;
    }

    @Override
    public String toString() {
        return "FilesFragment{" +
                "presenter=" + getPresenter().getClass().getSimpleName() +
                '}';
    }

    @Override
    public void onRefresh() {
        getPresenter().loadData(true, true, false);
    }

    @Subscribe
    public void onRenameEvent(RenameEvent event){
        if (mListDataSet != null) {
            for (int i = 0 ;i < mListDataSet.size(); i++) {
                if (mListDataSet.get(i).getItemId() == event.getItem().getItemId()) {
                    mListDataSet.set(i, event.getItem());
                    mRecyclerView.getAdapter().notifyItemChanged(i);
                }
            }
        }
    }

    @Subscribe
    public void onDeleteEvent(DeleteEvent event){
        if (mListDataSet != null) {
            for (int i = 0 ;i < mListDataSet.size(); i++) {
                if (mListDataSet.get(i).getItemId() == event.getItem().getItemId() && event.getItem().getDeletedAt() != null) {
                    mListDataSet.remove(i);
                    mRecyclerView.getAdapter().notifyItemRemoved(i);
                }
            }
        }
    }

}

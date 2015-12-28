package com.tevinjeffrey.vapor.ui.files.fragments;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.VaprApp;
import com.tevinjeffrey.vapor.ui.files.adapters.FilesFragmentAdapter;
import com.tevinjeffrey.vapor.events.DeleteEvent;
import com.tevinjeffrey.vapor.events.RenameEvent;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType;
import com.tevinjeffrey.vapor.ui.base.Presenter;
import com.tevinjeffrey.vapor.ui.base.View;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.ArchivePresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.AudioPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BookmarkPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.FilesPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.ImagePresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.RecentPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.TextPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.UnknownPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.VideoPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.views.FilesView;
import com.tevinjeffrey.vapor.ui.login.LoginException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FilesFragment extends MVPFragment implements FilesView, SwipeRefreshLayout.OnRefreshListener {
    private static final String ITEM_TYPE = "ITEM_TYPE";
    @Bind(R.id.files_recyclerview)
    RecyclerView mRecyclerView;
    @Bind(R.id.empty_view)
    LinearLayout mEmptyView;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Inject
    Bus bus;

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
        VaprApp.objectGraph(getParentActivity()).inject(this);
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
        initRecyclerView();
        initSwipeLayout();
        mPresenter.attachView(this);
        if (!getPresenter().isLoading()) {
            getPresenter().loadData(true, false);
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

    public void setPresenter(Presenter<View> filesPresenter) {
        mPresenter = filesPresenter;
        VaprApp.objectGraph(getParentActivity()).inject(mPresenter);
    }

    @Override
    public void showLoading(final boolean pullToRefresh) {
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
        mListDataSet.clear();
        mListDataSet.addAll(data);
        mRecyclerView.getAdapter().notifyDataSetChanged();
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
            t.printStackTrace();
            message = getString(R.string.error_occurred);
        }
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    public void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getParentActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        if (mListDataSet == null) {
            mListDataSet = new ArrayList<>(100);
        }

        if (mRecyclerView.getAdapter() == null) {
            mRecyclerView.setAdapter(new FilesFragmentAdapter(mListDataSet, getParentActivity()));
        }
    }

    public void initSwipeLayout() {
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);
    }

    @Override
    public void showLayout(LayoutType type) {
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

    private Presenter<View> getTypedPresenter() {
        Presenter<View> presenter;
        switch (mItemType) {
            case ALL:
                presenter = VaprApp.objectGraph(getParentActivity()).get(RecentPresenter.class);
                break;
            case IMAGE:
                presenter = VaprApp.objectGraph(getParentActivity()).get(ImagePresenter.class);
                break;
            case VIDEO:
                presenter = VaprApp.objectGraph(getParentActivity()).get(VideoPresenter.class);
                break;
            case ARCHIVE:
                presenter = VaprApp.objectGraph(getParentActivity()).get(ArchivePresenter.class);
                break;
            case TEXT:
                presenter = VaprApp.objectGraph(getParentActivity()).get(TextPresenter.class);
                break;
            case AUDIO:
                presenter = VaprApp.objectGraph(getParentActivity()).get(AudioPresenter.class);
                break;
            case BOOKMARK:
                presenter = VaprApp.objectGraph(getParentActivity()).get(BookmarkPresenter.class);
                break;
            case UNKNOWN:
                presenter = VaprApp.objectGraph(getParentActivity()).get(UnknownPresenter.class);
                break;
            default:
                presenter = VaprApp.objectGraph(getParentActivity()).get(RecentPresenter.class);
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
        getPresenter().loadData(true, true);
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

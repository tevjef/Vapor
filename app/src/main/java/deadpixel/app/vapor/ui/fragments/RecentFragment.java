package deadpixel.app.vapor.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import deadpixel.app.vapor.R;
import deadpixel.app.vapor.adapter.FilesListViewAdapter;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.database.FilesManager;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.libs.EaseOutQuint;
import deadpixel.app.vapor.ui.ImageViewActivity;
import deadpixel.app.vapor.ui.MainActivity;
import deadpixel.app.vapor.ui.intefaces.FilesFragment;
import deadpixel.app.vapor.utils.AppUtils;

public class RecentFragment extends Fragment implements FilesFragment, AbsListView.OnScrollListener, AbsListView.OnItemClickListener{

    FilesListViewAdapter adapter;

    ListView mListView;


    //A flag for when the fragment is in the process of getting more files after an onScroll event


    //Fragment optimistically assumes there is more files on startup up. False when a get from database returns 0 files
    public boolean moreFiles = true;

    boolean fullySynced;

    boolean userScrolled = false;
    final int AUTOLOAD_THRESHOLD = 4;
    public boolean autoLoadFiles;

    ArrayList<DatabaseItem> items;

    View noFiles;
    View refreshingFiles;
    //View loadingMoreFiles;

    FrameLayout footerFrameLayout;
    FrameLayout headerFrameLayout;

    AnimationDrawable anim;
    private boolean firstStart;
    private String fragmentType;
    private boolean isLoading;

    public CloudAppItem.Type getType() {
        return mType;
    }

    public void setType(CloudAppItem.Type mType) {
        this.mType = mType;
    }

    CloudAppItem.Type mType = CloudAppItem.Type.ALL;



    private State mState;


    public RecentFragment() {
    }

    public ListView getListView() {
        return mListView;
    }

    public ListAdapter getListAdapter() {
        return adapter;
    }

    public void setListAdapter(FilesListViewAdapter listAdapter) {
        getListView().setAdapter(listAdapter);
    }


    private enum State {
        NO_FILES, REFRESHING, NORMAL, LOADING_MORE
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();

        autoLoadFiles = arguments.getBoolean(AUTOLOAD, true);
        fragmentType = arguments.getString(FRAGMENT_TYPE);

        firstStart = AppUtils.mPref.getBoolean(AppUtils.APP_FIRST_START, true);

        if(firstStart && !isLoading) {
            isLoading = true;
            FilesManager.requestMoreFiles(CloudAppItem.Type.ALL);
        }

        mListView = (ListView) getActivity().findViewById(R.id.files_fragment_list_view);

        footerFrameLayout = new FrameLayout(getActivity());
        headerFrameLayout = new FrameLayout(getActivity());

        AbsListView.LayoutParams paramsMatchParent = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        footerFrameLayout.setLayoutParams(paramsMatchParent);
        footerFrameLayout.setForegroundGravity(Gravity.CENTER);
        headerFrameLayout.setLayoutParams(paramsMatchParent);
        headerFrameLayout.setForegroundGravity(Gravity.CENTER);

        noFiles = getActivity().getLayoutInflater().inflate(R.layout.no_files, null);
        refreshingFiles = getActivity().getLayoutInflater().inflate(R.layout.refreshing, null);
        footerFrameLayout = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.loading_more, null);

        anim = (AnimationDrawable) footerFrameLayout.findViewById(R.id.image).getBackground();
        anim.start();
        anim = (AnimationDrawable) refreshingFiles.findViewById(R.id.image).getBackground();
        anim.start();

    }


    private void setFooterVisibility(int i) {
        if(i == 0) {
            footerFrameLayout.setVisibility(View.GONE);
        } else if(i == 1) {
            footerFrameLayout.setVisibility(View.VISIBLE);
        }
    }

    private void resetFooter() {

        footerFrameLayout.setVisibility(View.VISIBLE);
        final View textContainer = footerFrameLayout.findViewById(R.id.load_more_text_container);
        final View btnContainer = footerFrameLayout.findViewById(R.id.load_more_button_container);
        final Button loadMoreButton = (Button) footerFrameLayout.findViewById(R.id.load_more_button);


        if(!fullySynced) {

            if (autoLoadFiles) {
                textContainer.setVisibility(View.VISIBLE);
                btnContainer.setVisibility(View.GONE);
            } else {

                YoYo.with(Techniques.FadeOutDown).duration(250).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        textContainer.setVisibility(View.GONE);

                        btnContainer.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInUp).duration(250).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                btnContainer.setEnabled(false);

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                btnContainer.setEnabled(true);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).playOn(btnContainer);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).playOn(textContainer);



                loadMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        YoYo.with(Techniques.FadeOutDown).duration(250).interpolate(new EaseOutQuint()).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                textContainer.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInUp).duration(250).interpolate(new EaseOutQuint()).playOn(textContainer);

                                loadMoreFiles();
                            }
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                btnContainer.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).playOn(btnContainer);

                    }
                });

                //addFooterView(loadingMoreFiles);
            }
        } else {
            setFooterVisibility(0);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setBackgroundResource(0);
        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        getListView().setFastScrollEnabled(true);

        items = new ArrayList<DatabaseItem>();
        //FilesManager.getFiles(CloudAppItem.Type.ALL);


        adapter = new FilesListViewAdapter(getActivity(),items);
        getListView().setOnScrollListener(this);


        getListView().setAdapter(null);
        getListView().addHeaderView(headerFrameLayout);
        getListView().addFooterView(footerFrameLayout);
        //addFooterView(loadingMoreFiles);
        resetFooter();
        //The initial files get is handled by the parent activity of this fragment.
        //adds the refreshing view's View on app first load to tell users the app is doing work.
        if (AppUtils.mPref.getBoolean(AppUtils.APP_FIRST_START, true) || isLoading ) {
            updateState(State.REFRESHING);
        } else {
            //Get all items in the database for this fragment.
            new GetAllItemsTask().execute();
            updateState(State.NORMAL);
        }

        setListAdapter(adapter);

        getListView().setSelector(android.R.color.transparent);

    }


    protected void addHeaderView(View v) {
        headerFrameLayout.removeAllViews();
        headerFrameLayout.addView(v);
    }

    protected void removeHeaderViews() {
        headerFrameLayout.removeAllViews();
    }

    @Override
    public void datebaseUpdateEvent(ArrayList<DatabaseItem> items) {

        //Gets all items added when runs first even though all items would be in the parameter.
        if(mState == State.REFRESHING) {
            new GetAllItemsTask().execute();
        } else {
            //Appends items to list when users scrolls to the bottom.
            this.addItemsToAdapter(sortListByType(getType(), items));
        }

    }

    @Override
    public void errorEvent() {
        updateState(State.NORMAL);
        new GetAllItemsTask().execute();
        isLoading = false;
        resetFooter();
    }

    @Override
    public void refresh() {
        updateState(State.REFRESHING);
        //isLoading =true;
        FilesManager.refreshFiles();
    }

    protected void addItemsToAdapter(ArrayList<DatabaseItem> items) {

        //Loading is done list can be scrolled
        isLoading = false;

        fullySynced = AppUtils.mPref.getBoolean(AppUtils.FULLY_SYNCED, false);


        //If item size is zero, there's not more files.
        if(items.size() == 0) {

            // If the adapter has no files at this point, the user might not have any files.
            if(getListAdapter().getCount() == 0 && fullySynced) {
                updateState(State.NO_FILES);
            } else {
                updateState(State.NORMAL);
                resetFooter();
            }


        } else {

            updateState(State.NORMAL);


            adapter.addAll(items);
            notifyChangeInAdapter();

            resetFooter();
        }


    }

    public FilesListViewAdapter getAdapter() {
        return adapter;
    }

    protected ArrayList<DatabaseItem> sortListByType(CloudAppItem.Type type, ArrayList<DatabaseItem> items) {
        ArrayList<DatabaseItem> typedList = new ArrayList<DatabaseItem>();
        if(type == CloudAppItem.Type.ALL) {
            typedList = items;
        } else {
            for (DatabaseItem i : items) {
                if (i.getItemType() == type) {
                    typedList.add(i);
                }
            }
        }
        return typedList;
    }

    protected void notifyChangeInAdapter() {
        if(getListAdapter() != null) {
            adapter.notifyDataSetChanged();
        }
    }

    protected void clearAdapter() {
        if(getListAdapter() != null) {
            adapter.clear();
            notifyChangeInAdapter();
        }
    }



    public class GetAllItemsTask extends AsyncTask<Void, Void, ArrayList<DatabaseItem>> {
        @Override
        protected ArrayList<DatabaseItem> doInBackground(Void... params) {
            return FilesManager.getFiles(getType());
        }
        @Override
        protected void onPostExecute(ArrayList<DatabaseItem> databaseItems) {
            addItemsToAdapter(databaseItems);
        }
    }


    private void loadMoreFiles() {
        isLoading = true;
        updateState(State.LOADING_MORE);
        FilesManager.requestMoreFiles(CloudAppItem.Type.ALL);
    }

    protected void updateState(State state) {
        mState  = state;

        switch(mState) {
            case NO_FILES:
                if(getListView() != null) {
                    addHeaderView(noFiles);
                    footerFrameLayout.setVisibility(View.GONE);
                }

                break;
            case REFRESHING:
                if(getListView() != null) {
                    addHeaderView(refreshingFiles);
                    if (getListView().getAdapter() != null) {
                        clearAdapter();
                    }
                    footerFrameLayout.setVisibility(View.GONE);
                }
                isLoading = true;
                break;
            case NORMAL:
                removeHeaderViews();
                break;
            case LOADING_MORE:

                break;
            default:
                break;
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case SCROLL_STATE_TOUCH_SCROLL:
                userScrolled = true;
                break;
            case SCROLL_STATE_IDLE:
                userScrolled = false;
                break;
            case SCROLL_STATE_FLING:
                userScrolled = true;
                break;
            default:
                userScrolled = false;
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if((firstVisibleItem + visibleItemCount) >= (totalItemCount - AUTOLOAD_THRESHOLD)
                && !isLoading
                && visibleItemCount != 0
                && mState != State.NO_FILES
                && mState != State.REFRESHING
                && !fullySynced
                && userScrolled) {

            if(autoLoadFiles) {
                loadMoreFiles();
            }

        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getCount() != 0) {
            DatabaseItem dbItem = (DatabaseItem) parent.getAdapter().getItem(position);
            String name = dbItem.getName();

            switch (dbItem.getItemType()) {
                case IMAGE:
                    Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                    intent.putExtra(EXTRA_NAME, name);
                    startActivity(intent);

                    break;
                default:
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(dbItem.getContentUrl()));
                    startActivity(i);

            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) getActivity()).setCurrentFragment(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
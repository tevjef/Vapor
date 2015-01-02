package deadpixel.app.vapor;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

import org.apache.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.ArrayList;

import deadpixel.app.vapor.adapter.FilesListViewAdapter;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.callbacks.ResponseEvent;
import deadpixel.app.vapor.database.FilesManager;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.networkOp.DatabaseTaskFragment;
import deadpixel.app.vapor.utils.AppUtils;

import static deadpixel.app.vapor.cloudapp.api.model.CloudAppItem.*;

public class MainActivity extends SherlockFragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, DatabaseTaskFragment.TaskCallbacks{

    private static final String TAG_TASK_FRAGMENT = "Database_task_fragment";
    private static final String TAG_ALL_FRAGMENT = "all_fragment";
    private static final String TAG_IMAGE_FRAGMENT = "image_fragment";
    private static final String TAG_VIDEO_FRAGMENT = "video_fragment";
    private static final String TAG_AUDIO_FRAGMENT = "audio_fragment";
    private static final String TAG_TEXT_FRAGMENT = "text_fragment";
    private static final String TAG_ARCHIVE_FRAGMENT = "archive_fragment";
    private static final String TAG_BOOKMARK_FRAGMENT = "bookmark_fragment";
    private static final String TAG_OTHER_FRAGMENT = "other_fragment";
    private static final String TAG_TRASH_FRAGMENT = "trash_fragment";

    final public static String EXTRA_NAME = "item_name";

    private AnimationDrawable refreshIcon;
    private MenuItem refreshMenuItem;


    private DatabaseTaskFragment mDatabaseTaskFragment;


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public Typeface mNormal, mBold;
    private String[] actionBarTitles;
    private TypedArray titleIcons;
    private ActionBar ab;

    public static boolean isLoading = false;

    private static boolean isSyncing = false;
    static boolean firstStart = true;


    FragmentTransaction ft;

    AllFragment allFragment;
    ImageFragment imageFragment;
    VideoFragment videoFragment;
    AudioFragment audioFragment;
    TextFragment textFragment;
    ArchiveFragment archiveFragment;
    BookmarkFragment bookMarkFragment;
    OtherFragment otherFragment;
    TrashFragment trashFragment;

    public static FilesFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();

        ab = getSupportActionBar();

        // load icons from resources
        titleIcons = getResources()
                .obtainTypedArray(R.array.file_icons_white);



        allFragment = new AllFragment();
        imageFragment = new ImageFragment();
        videoFragment = new VideoFragment();
        audioFragment = new AudioFragment();
        textFragment = new TextFragment();
        archiveFragment = new ArchiveFragment();
        bookMarkFragment = new BookmarkFragment();
        otherFragment = new OtherFragment();
        trashFragment = new TrashFragment();

        FragmentManager fm = getSupportFragmentManager();

        mDatabaseTaskFragment = (DatabaseTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mDatabaseTaskFragment == null) {
            mDatabaseTaskFragment = new DatabaseTaskFragment();
            fm.beginTransaction().add(mDatabaseTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    private void setPaddingOnHome() {
        ImageView view = (ImageView)findViewById(android.R.id.home);
        int size = 3;
        int padding = (int) (getResources().getDisplayMetrics().density * size);
        view.setPadding(padding , padding, padding, padding);
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {

        configureActionBar(position);
        // update the main content by replacing fragments



        switch (position) {
            case 0:
                ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in_up, R.anim.activity_close_exit);
                ft.replace(R.id.container, allFragment)
                        .commit();


                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
                break;
            case 1:
                ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in_up, R.anim.activity_close_exit);
                ft.replace(R.id.container, imageFragment)
                        .commit();

                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
                break;
            case 2:
                ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in_up, R.anim.activity_close_exit);
                ft.replace(R.id.container, videoFragment)
                        .commit();

                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
                break;
            case 3:
                ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in_up, R.anim.activity_close_exit);
                ft.replace(R.id.container, audioFragment)
                        .commit();

                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
                break;
            case 4:
                ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in_up, R.anim.activity_close_exit);
                ft.replace(R.id.container, textFragment)
                        .commit();

                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
                break;
            case 5:
                ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in_up, R.anim.activity_close_exit);
                ft.replace(R.id.container, archiveFragment)
                        .commit();

                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
                break;
            case 6:
                ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in_up, R.anim.activity_close_exit);
                ft.replace(R.id.container, bookMarkFragment)
                        .commit();

                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
                break;
            case 7:
                ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in_up, R.anim.activity_close_exit);
                ft.replace(R.id.container, otherFragment)
                        .commit();

                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
                break;
            case 8:
                ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in_up, R.anim.activity_close_exit);
                ft.replace(R.id.container, trashFragment)
                        .commit();

                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
                break;

            default:
                break;
        }
    }

    public void onSectionAttached(int number) {

    }




    /**
     * An implemented callback function to handle the action bar title and
     * icon when the drawer is closed
     */
    @Override
    public void restoreActionBarTitle(int position) {
        configureActionBar(position);
    }

    public void configureActionBar(int position){

        // load titles from resources
        actionBarTitles = getResources().getStringArray(R.array.nav_drawer_items);
        ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ab.setDisplayShowTitleEnabled(true);

        boolean b = AppUtils.mPref.getBoolean("navigation_drawer_learned", false);
        if (position > -1 && b) {
            mTitle = actionBarTitles[position];
            ab.setTitle(mTitle);
            ab.setIcon(titleIcons.getResourceId(position, -1));

        } else {
            ab.setIcon(R.drawable.ic_home);
            ab.setTitle(R.string.library);
        }

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        refreshMenuItem = menu.findItem(R.id.main_refresh);

        if(refreshMenuItem != null) {
            refreshMenuItem.setIcon(R.drawable.cloud_refresh);
            refreshIcon = (AnimationDrawable) refreshMenuItem.getIcon();
        }
        return super.onPrepareOptionsMenu(menu);
    }


    public void setOkIcon() {
        if(refreshMenuItem != null) {
            refreshMenuItem.setIcon(R.drawable.cloud_ok);
            refreshIcon = (AnimationDrawable) refreshMenuItem.getIcon();
            refreshIcon.setOneShot(true);

            int duration = 0;

            for (int i = 0; i <= refreshIcon.getNumberOfFrames() - 1; i++) {
                duration += refreshIcon.getDuration(i);
            }

            refreshIcon.start();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Don't want this being called if user has already left the activity
                    if (!isDestroyed() || !isFinishing()) {
                        //calls onPrepareOptions menu. Effectively resetting the actionbar.
                        supportInvalidateOptionsMenu();
                    }
                }
            }, duration);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getSupportMenuInflater().inflate(R.menu.main, menu);
            // Associate searchable configuration with the SearchView
            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView =
                    (SearchView) menu.findItem(R.id.main_search).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));

            styleSearchView(searchView);

            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void styleSearchView(SearchView searchView) {
        if(searchView != null) {
            //Search Text Color
            int queryTextViewId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            AutoCompleteTextView queryTextView = (AutoCompleteTextView) searchView.findViewById(queryTextViewId);
            queryTextView.setTextColor(getResources().getColor(R.color.white));

            queryTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
            queryTextView.setSingleLine(true);
            //queryTextView.setTextAppearance(queryTextView.getContext(), R.style.Text_Light);
            queryTextView.setCompoundDrawables(null ,null ,null ,null);
            queryTextView.setCompoundDrawablePadding(0);
            queryTextView.setHintTextColor(getResources().getColor(R.color.text_highlight_color));
            queryTextView.setBackgroundResource(R.drawable.vapor_edit_text_holo_light);


            //Search plate
            int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
            View searchPlate = searchView.findViewById(searchPlateId);
            searchPlate.setBackgroundResource(android.R.color.transparent);

            LinearLayout frame = (LinearLayout) searchPlate.getParent();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(8, 4, 8, -4);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            frame.setLayoutParams(layoutParams);

            ImageView searchHintIcon = (ImageView) frame.getChildAt(0);

            if(searchHintIcon != null) {
                searchHintIcon.setVisibility(View.GONE);
            }

            Field searchField = null;
            try {
                searchField = SearchView.class.getDeclaredField("mCloseButton");

                searchField.setAccessible(true);
                ImageView closeBtn = (ImageView) searchField.get(searchView);
                closeBtn.setImageResource(R.drawable.ic_close_white);


                searchField = SearchView.class.getDeclaredField("mSearchHintIcon");
                searchField.setAccessible(true);
                ImageView hintIcon = (ImageView) searchField.get(searchView);
                hintIcon.setImageResource(R.drawable.ic_search_white);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if(!isLoading) {
            int id = item.getItemId();
//TODO add functionality for mainactivity menu items here by getting their ids


            if (id == R.id.main_refresh) {
                if (currentFragment != null) {
                    currentFragment.refresh();
                }
                refreshIcon.start();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        titleIcons.recycle();
        super.onDestroy();

    }


    @Override
    public void onDatabaseUpdate(ArrayList<DatabaseItem> items) {
        setOkIcon();
        //if it delvers items items it's no longer first start up.
        AppUtils.mPref.edit().putBoolean(AppUtils.APP_FIRST_START, false).commit();
        //delivers items after a request for new items from server
        if(currentFragment != null) {
            currentFragment.datebaseUpdateEvent(items);
        }



    }

    @Override
    public void onServerResponse(ResponseEvent event) {

    }

    @Override
    public void onErrorEvent(ErrorEvent errorEvent) {
        AppUtils.makeCrouton(this, getErrorDescription(errorEvent), AppUtils.Style.ALERT);
        if(currentFragment != null) {
            currentFragment.errorEvent();
        }
    }

    //Takes a guess as to what error type is given the particular
    // state of the application, signin, register or forgot password, though not
    //implemented since it's not needed in this case
    private String getErrorDescription(ErrorEvent error) {
        String errorDescription;

        if(!error.getErrorDescription().equals(AppUtils.NO_CONNECTION)) {
            if (error.getError().networkResponse == null) {
                errorDescription = "There's a problem contacting CloudApp servers";
            } else {
                switch (error.getStatusCode()) {
                    case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                        errorDescription = ((Integer)HttpStatus.SC_UNPROCESSABLE_ENTITY).toString();
                        break;
                    case HttpStatus.SC_UNAUTHORIZED:
                        errorDescription = "Your email and password are no longer valid. Please logout";
                        break;
                    case HttpStatus.SC_NOT_ACCEPTABLE:
                        errorDescription = ((Integer)HttpStatus.SC_NOT_ACCEPTABLE).toString();
                        break;
                    case HttpStatus.SC_NO_CONTENT:
                        errorDescription = "That email belongs to another user";
                        break;
                    default:
                        errorDescription = getResources().getString(R.string.error_occurred);
                }
            }
        }   else {
            errorDescription = getResources().getString(R.string.check_internet);
        }
        return errorDescription;
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class RecentFragment extends ListFragment implements FilesFragment, AbsListView.OnScrollListener{

        FilesListViewAdapter adapter;

        //A flag for when the fragment is in the process of getting more files after an onScroll event


        //Fragment optimistically assumes there is more files on startup up. False when a get from database returns 0 files
        public boolean moreFiles = true;

        boolean fullySynced;

        boolean userScrolled = false;
        final int AUTOLOAD_THRESHOLD = 4;
        public boolean mAutoLoad;

        ArrayList<DatabaseItem> items;

        View noFiles;
        View refreshingFiles;
        //View loadingMoreFiles;

        FrameLayout footerFrameLayout;
        FrameLayout headerFrameLayout;

        AnimationDrawable anim;

        public Type getType() {
            return mType;
        }

        public void setType(Type mType) {
            this.mType = mType;
        }

        Type mType = Type.ALL;



        private State mState;


        public RecentFragment() {
        }

        private enum State {
            NO_FILES, REFRESHING, NORMAL, LOADING_MORE
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            firstStart = AppUtils.mPref.getBoolean(AppUtils.APP_FIRST_START, true);

            if(firstStart && !isLoading) {
                isLoading = true;
                FilesManager.requestMoreFiles(Type.ALL);
            }

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

                if (mAutoLoad) {
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

                final ArrayAdapter adapter = (ArrayAdapter) getListAdapter();
                adapter.addAll(items);
                notifyChangeInAdapter();

                resetFooter();
            }


        }

        public FilesListViewAdapter getAdapter() {
            return adapter;
        }

        protected ArrayList<DatabaseItem> sortListByType(Type type, ArrayList<DatabaseItem> items) {
            ArrayList<DatabaseItem> typedList = new ArrayList<DatabaseItem>();
            if(type == Type.ALL) {
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
                final ArrayAdapter adapter = (ArrayAdapter) getListAdapter();
                adapter.notifyDataSetChanged();
            }
        }

        protected void clearAdapter() {
            if(getListAdapter() != null) {
                final ArrayAdapter adapter = (ArrayAdapter) getListAdapter();
                adapter.clear();
                notifyChangeInAdapter();
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

                if(mAutoLoad) {
                    loadMoreFiles();
                }

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
            FilesManager.requestMoreFiles(Type.ALL);
        }

        @Override
        public void onListItemClick(final ListView l, final View v, int position, long id) {

            if(l.getAdapter().getCount() != 0) {
                DatabaseItem dbItem = (DatabaseItem) l.getAdapter().getItem(position);
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
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            currentFragment = this;
        }

        @Override
        public void onDetach() {
            super.onDetach();
        }
    }
    /**
     * A placeholder fragment containing a simple view.
     */

    public static class AllFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

        public AllFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.mAutoLoad = true;
            super.setType(Type.ALL);
            super.onCreate(savedInstanceState);
        }
    }

    public static class ImageFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

        public ImageFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.mAutoLoad = false;
            super.setType(Type.IMAGE);
            super.onCreate(savedInstanceState);

        }
    }

    public static class VideoFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

        public VideoFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.mAutoLoad = false;
            super.setType(Type.VIDEO);
            super.onCreate(savedInstanceState);
        }
    }

    public static class AudioFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

        public AudioFragment() {}


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.mAutoLoad = false;
            super.setType(Type.AUDIO);
            super.onCreate(savedInstanceState);
        }
    }

    public static class TextFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

        public TextFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.mAutoLoad = false;
            super.setType(Type.TEXT);
            super.onCreate(savedInstanceState);
        }
    }

    public static class ArchiveFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

        public ArchiveFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.mAutoLoad = false;
            super.setType(Type.ARCHIVE);
            super.onCreate(savedInstanceState);
        }
    }

    public static class BookmarkFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

        public BookmarkFragment() {}


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.mAutoLoad = false;
            super.setType(Type.BOOKMARK);
            super.onCreate(savedInstanceState);
        }
    }

    public static class OtherFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

        public OtherFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.mAutoLoad = false;
            super.setType(Type.UNKNOWN);
            super.onCreate(savedInstanceState);
        }
    }

    public static class TrashFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

        public TrashFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.mAutoLoad = false;
            super.setType(Type.DELETED);
            super.onCreate(savedInstanceState);
        }
    }

    public static Type parseType(String s) {

        if (s.equals("ALL")) {
            return Type.ALL;

        } else if (s.equals("IMAGE")) {
            return Type.IMAGE;

        } else if (s.equals("VIDEO")) {
            return Type.VIDEO;

        } else if (s.equals("AUDIO")) {
            return Type.AUDIO;

        } else if (s.equals("TEXT")) {
            return Type.TEXT;

        } else if (s.equals("ARCHIVE")) {
            return Type.ARCHIVE;

        } else if (s.equals("BOOKMARK")) {
            return Type.BOOKMARK;

        } else if (s.equals("UNKNOWN")) {
            return Type.UNKNOWN;

        } else if (s.equals("DELETED")) {
            return Type.DELETED;

        } else {
            return Type.ALL;

        }

    }
    private interface FilesFragment {
        public void datebaseUpdateEvent(ArrayList<DatabaseItem> items);
        public void errorEvent();
        public void refresh();
    }


}

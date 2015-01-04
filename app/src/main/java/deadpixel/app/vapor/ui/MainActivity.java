package deadpixel.app.vapor.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

import org.apache.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.ArrayList;

import deadpixel.app.vapor.R;
import deadpixel.app.vapor.adapter.FilesListViewAdapter;
import deadpixel.app.vapor.adapter.NavDrawerListAdapter;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.callbacks.ResponseEvent;
import deadpixel.app.vapor.database.FilesManager;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.libs.EaseOutQuint;
import deadpixel.app.vapor.model.NavDrawerItem;
import deadpixel.app.vapor.networkOp.DatabaseTaskFragment;
import deadpixel.app.vapor.ui.factory.FilesFragmentFactory;
import deadpixel.app.vapor.ui.fragments.AllFilesFragment;
import deadpixel.app.vapor.ui.fragments.ArchiveFragment;
import deadpixel.app.vapor.ui.fragments.AudioFragment;
import deadpixel.app.vapor.ui.fragments.BookmarkFragment;
import deadpixel.app.vapor.ui.fragments.ImageFragment;
import deadpixel.app.vapor.ui.fragments.OtherFragment;
import deadpixel.app.vapor.ui.fragments.RecentFragment;
import deadpixel.app.vapor.ui.fragments.TextFragment;
import deadpixel.app.vapor.ui.fragments.TrashFragment;
import deadpixel.app.vapor.ui.fragments.VideoFragment;
import deadpixel.app.vapor.ui.intefaces.FilesFragment;
import deadpixel.app.vapor.utils.AppUtils;

import static deadpixel.app.vapor.cloudapp.api.model.CloudAppItem.Type;

public class MainActivity extends ActionBarActivity
        implements DatabaseTaskFragment.TaskCallbacks, AdapterView.OnItemClickListener {



    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView leftDrawerList;

    private View mDrawerContainerView ;

    private NavDrawerListAdapter adapter;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIconsGrey;
    private TypedArray navMenuIconsBlue;

    private static final String TAG = "MainActivity";

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



    //private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;
    public Typeface mNormal, mBold;
    private String[] actionBarTitles;
    private TypedArray titleIcons;
    private ActionBar ab;
    private FilesFragmentFactory filesFragmentFactory;
    public static boolean isLoading = false;

    private static boolean isSyncing = false;
    static boolean firstStart = true;


    FragmentTransaction ft;


    public static FilesFragment currentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();

        ab = getSupportActionBar();

        // load icons from resources
        titleIcons = getResources()
                .obtainTypedArray(R.array.file_icons_white);


        createFilesFragmentFactory();



        FragmentManager fm = getSupportFragmentManager();

        mDatabaseTaskFragment = (DatabaseTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mDatabaseTaskFragment == null) {
            mDatabaseTaskFragment = new DatabaseTaskFragment();
            fm.beginTransaction().add(mDatabaseTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        initView();
        if (toolbar != null) {
            toolbar.setTitle("Navigation Drawer");
            setSupportActionBar(toolbar);
        }

    }

    public void initView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftDrawerList = (ListView) findViewById(R.id.drawer_listview);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        mDrawerContainerView = findViewById(R.id.drawer_container);
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIconsBlue = getResources()
                .obtainTypedArray(R.array.file_icons_blue);

        // nav drawer icons from resources
        navMenuIconsGrey = getResources()
                .obtainTypedArray(R.array.file_icons_grey);

        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<>();

        // adding nav drawer items to array
        // Recent Files
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIconsGrey.getResourceId(0, -1)));
        // Images
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIconsGrey.getResourceId(1, -1)));
        // Video
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIconsGrey.getResourceId(2, -1)));
        // Audio
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIconsGrey.getResourceId(3, -1)));
        // Text
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIconsGrey.getResourceId(4, -1)));
        // Archives
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIconsGrey.getResourceId(5, -1)));
        // Bookmarks
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIconsGrey.getResourceId(6, -1)));
        // Other
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIconsGrey.getResourceId(7, -1)));
        // Trash
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIconsGrey.getResourceId(8, -1)));

        // Recycle the typed array
        navMenuIconsGrey.recycle();

        adapter = new NavDrawerListAdapter(this, navDrawerItems, 0);

        leftDrawerList.setAdapter(adapter);

        leftDrawerList.setOnItemClickListener(this);

        initDrawer();

    }




    private void initDrawer() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onNavigationDrawerItemSelected(position);
    }

    public boolean isDrawerOpen() {
        return false;
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
        void restoreActionBarTitle(int position);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    private void createFilesFragmentFactory() {
        filesFragmentFactory = new FilesFragmentFactory();
    }

    private void logError(Exception e) {
        Log.e(TAG, e.getMessage());
    }

    public void onNavigationDrawerItemSelected(int position) {

        configureActionBar(position);
        // update the main content by replacing fragments

        switch (position) {
            case 0:
               replaceFragment(FilesFragment.ALL_FRAGMENT);
                break;
            case 1:
                replaceFragment(FilesFragment.IMAGE_FRAGMENT);
                break;
            case 2:
                replaceFragment(FilesFragment.VIDEO_FRAGMENT);
                break;
            case 3:
                replaceFragment(FilesFragment.AUDIO_FRAGMENT);
                break;
            case 4:
                replaceFragment(FilesFragment.TEXT_FRAGMENT);
                break;
            case 5:
                replaceFragment(FilesFragment.ARCHIVE_FRAGMENT);
                break;
            case 6:
                replaceFragment(FilesFragment.BOOKMARK_FRAGMENT);
                break;
            case 7:
                replaceFragment(FilesFragment.OTHER_FRAGMENT);
                break;
            case 8:
                replaceFragment(FilesFragment.TRASH_FRAGMENT);
                break;

            default:
                break;
        }
    }

    private void replaceFragment(String fragmentType) {
        try {
            ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.fade_in_up, R.anim.activity_close_exit);
            ft.replace(R.id.container, filesFragmentFactory.makeFragment(fragmentType), fragmentType)
                    .commit();
            supportInvalidateOptionsMenu();
        } catch (Exception e) {
            logError(e);
        }
    }

    /**
     * An implemented callback function to handle the action bar title and
     * icon when the drawer is closed
     */

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
        /*if (!isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            // Associate searchable configuration with the SearchView
            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView =
                    (SearchView) menu.findItem(R.id.main_search).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));

            styleSearchView(searchView);

            return true;
        }*/


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

        switch (item.getItemId()) {
            case android.R.id.home:
                if(drawerLayout.isDrawerOpen(mDrawerContainerView)) {
                    drawerLayout.closeDrawer(mDrawerContainerView);
                }
                else {
                    drawerLayout.openDrawer(mDrawerContainerView);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

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

    public void setCurrentFragment(FilesFragment fragment) {
        currentFragment = fragment;
    }
}

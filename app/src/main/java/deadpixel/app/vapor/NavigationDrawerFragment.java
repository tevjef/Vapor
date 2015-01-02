package deadpixel.app.vapor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

import java.util.ArrayList;
import java.util.Arrays;

import deadpixel.app.vapor.adapter.NavDrawerListAdapter;
import deadpixel.app.vapor.model.NavDrawerItem;
import deadpixel.app.vapor.utils.AppUtils;

;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends SherlockFragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mPreviousSelectedPosition = -1;
    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;


    private NavDrawerListAdapter adapter;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIconsGrey;
    private TypedArray navMenuIconsBlue;

    private String[] drawerSubMenuTitles;
    private TypedArray drawerSubMenuIcons;


    public Typeface mNormal, mBold;
    private TextView tempTextView;
    private ImageView tempImageView;
    boolean drawerClosed;

    private Runnable mPendingRunnable;
    private Handler mHandler;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onDestroy() {
        navMenuIconsGrey.recycle();
        navMenuIconsBlue.recycle();

        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIconsBlue = getResources()
                .obtainTypedArray(R.array.file_icons_blue);

        // nav drawer icons from resources
        navMenuIconsGrey = getResources()
                .obtainTypedArray(R.array.file_icons_grey);

        // load slide menu items
        drawerSubMenuTitles = getResources().getStringArray(R.array.drawer_submenu_titles);

        // nav drawer icons from resources
        drawerSubMenuIcons = getResources()
                .obtainTypedArray(R.array.drawer_submenu_icons);


        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        mNormal = AppUtils.getTextStyle(AppUtils.TextStyle.LIGHT_NORMAL);
        mBold = AppUtils.getTextStyle(AppUtils.TextStyle.BOLD);

        drawerClosed = false;
        mHandler = new Handler();
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        View drawerSettings = v.findViewById(R.id.drawer_settings);
        drawerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSettings();
            }
        });
        View drawerAccount = v.findViewById(R.id.drawer_account);
        drawerAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAccount();
            }
        });
        mDrawerListView = (ListView) v.findViewById(R.id.drawer_listview);


        //if onCreate is calling mCurrentSelectedPosition fwith a value from savedInstanceState
        //it means the activity was recreate from a config change app restart
        if (!mFromSavedInstanceState) {
            // Select either the default item (0) or the last selected item.
            selectItem(mCurrentSelectedPosition);
        }

        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();

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

        adapter = new NavDrawerListAdapter(getActivity().getApplicationContext(), navDrawerItems, 0);
        mDrawerListView.setAdapter(adapter);


        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View v = parent.getChildAt(mCurrentSelectedPosition);
                if (v != null) {
                    tempTextView = (TextView) v.findViewById(R.id.drawer_listview_text);
                    tempImageView = (ImageView) v.findViewById(R.id.drawer_listview_icon);
                    //tempTextView.setTypeface(mNormal);
                    tempImageView.setImageResource(navMenuIconsGrey.getResourceId(mCurrentSelectedPosition, -1));
                    tempTextView.setTextColor(getResources().getColor(R.color.primary_text_color));

                }


                if(mCurrentSelectedPosition != position)
                    selectItem(position);


                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawer(mFragmentContainerView);
                }

                mCurrentSelectedPosition = position;



                tempTextView = (TextView) view.findViewById(R.id.drawer_listview_text);


                //tempTextView.setTypeface(mBold);

                tempImageView = (ImageView) parent.getChildAt(mCurrentSelectedPosition).findViewById(R.id.drawer_listview_icon);
                tempImageView.setImageResource(navMenuIconsBlue.getResourceId(mCurrentSelectedPosition, -1));

                tempTextView.setTextColor(getResources().getColor(R.color.blue));

                parent.invalidate();
                view.invalidate();


            }
        });



        return v;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {

            @Override
            public void onDrawerSlide(View drawerView, float offset) {
                super.onDrawerSlide(drawerView, offset);

               // Log.i("Float offset" , Float.toString(offset));
/*                if(!isDrawerOpen()) {
                    mCallbacks.restoreActionBarTitle(-1);
                } else {
                    mCallbacks.restoreActionBarTitle(mCurrentSelectedPosition);
                }*/
                //getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                if(mCallbacks != null) {
                    mCallbacks.restoreActionBarTitle(mCurrentSelectedPosition);
                }

                setBold();

                getActivity().supportInvalidateOptionsMenu();// calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                if(mCallbacks != null) {

                    mCallbacks.restoreActionBarTitle(-1);
                }

                setBold();

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
                }

                getActivity().supportInvalidateOptionsMenu();//getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

        };


        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });


    }



    public void setBold() {
       //Sets the currently selected child bold
        tempTextView = (TextView) mDrawerListView.getChildAt(mCurrentSelectedPosition).findViewById(R.id.drawer_listview_text);
        tempImageView = (ImageView) mDrawerListView.getChildAt(mCurrentSelectedPosition).findViewById(R.id.drawer_listview_icon);

        tempImageView.setImageResource(navMenuIconsBlue.getResourceId(mCurrentSelectedPosition, -1));

        //tempTextView.setTypeface(mBold);
        tempTextView.setTextColor(getResources().getColor(R.color.blue));

    }

    private void selectItem(final int position) {

        if(mPreviousSelectedPosition != mCurrentSelectedPosition)
            mPreviousSelectedPosition = mCurrentSelectedPosition;

        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCallbacks != null) {
                    mCallbacks.onNavigationDrawerItemSelected(mCurrentSelectedPosition);
                }
            }
        }, 300);
    }



    private void launchSettings() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        }, 300);

    }

    private void launchAccount() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), AccountActivity.class);
                startActivity(intent);
            }
        }, 300);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            //inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            if (mDrawerLayout.isDrawerOpen(mFragmentContainerView)) {
                mDrawerLayout.closeDrawer(mFragmentContainerView);
            } else {
                mDrawerLayout.openDrawer(mFragmentContainerView);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //actionBar.setTitle(mTitle);
    }

    private ActionBar getActionBar() {
        return getSherlockActivity().getSupportActionBar();
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
}

package deadpixel.app.vapor;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private String[] actionBarTitles;
    private TypedArray titleIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        configureActionBar(position);
        // update the main content by replacing fragments

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        switch (position) {
            case 0:
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        ft.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
                break;
            case 1:
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ft.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();


                supportInvalidateOptionsMenu();
                break;
            case 2:
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ft.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();

                supportInvalidateOptionsMenu();
                break;
            case 3:
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ft.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();

                supportInvalidateOptionsMenu();
                break;
            case 4:
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ft.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();

                supportInvalidateOptionsMenu();
                break;
            case 5:
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ft.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();

                supportInvalidateOptionsMenu();
                break;
            case 6:
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ft.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();

                supportInvalidateOptionsMenu();
                break;
            case 7:
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ft.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();

                supportInvalidateOptionsMenu();
                break;
            case 8:
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ft.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();

                supportInvalidateOptionsMenu();
                break;

            default:
                break;
        }
    }

    public void onSectionAttached(int number) {

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setTitle(mTitle);
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

        // load icons from resources
        titleIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons_white);

        ActionBar ab = getSupportActionBar();

        if (position > -1) {
            mTitle = actionBarTitles[position];
            ab.setTitle(mTitle);
            ab.setIcon(titleIcons.getResourceId(position, -1));

        } else {
            ab.setIcon(R.drawable.ic_launcher);
            ab.setTitle(R.string.library);
        }

        titleIcons.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        //LIST VIEW CODE HERE
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));

            
        }
    }

}

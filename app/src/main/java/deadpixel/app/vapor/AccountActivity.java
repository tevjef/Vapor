package deadpixel.app.vapor;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import deadpixel.app.vapor.fragments.AccountMainFragment;
import deadpixel.app.vapor.utils.AppUtils;


public class AccountActivity extends SherlockFragmentActivity {

    protected ActionBar ab;


    FragmentTransaction ft;

    AccountMainFragment accountMainFragment = new AccountMainFragment();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);

        setupActionBar();

        if (savedInstanceState == null) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.ptr_layout, accountMainFragment);
            ft.commit();
        }


    }


    private void setupActionBar() {
        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(true);
        ab.setDisplayUseLogoEnabled(true);
        ab.setTitle(R.string.account_activity_title);
        ab.setIcon(getResources().getDrawable(R.drawable.ic_settings_white));

        int titleId = getResources().getIdentifier("action_bar_title", "id",
                "android");
        int subTitleId = getResources().getIdentifier("action_bar_subtitle", "id",
                "android");


        TextView tv = (TextView) findViewById(titleId);
        //tv.setTypeface(AppUtils.getTextStyle(AppUtils.TextStyle.LIGHT_NORMAL));
        tv = (TextView) findViewById(subTitleId);
        tv.setTypeface(AppUtils.getTextStyle(AppUtils.TextStyle.LIGHT_NORMAL));
        tv.setTextColor(getResources().getColor(R.color.text_highlight_color));

    }

    private void setPaddingOnHome() {
        ImageView view = (ImageView)findViewById(android.R.id.home);
        int size = 3;
        int padding = (int) (getResources().getDisplayMetrics().density * size);
        view.setPadding(padding , padding, padding, padding);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getSupportMenuInflater().inflate(R.menu.global, menu);

        return true;
    }

    public void performTransaction(final SherlockFragment frag) {
        new Thread(new Runnable() {


            @Override
            public void run() {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                ft.replace(R.id.ptr_layout, frag);
                ft.addToBackStack(null);
                ft.commit();
            }
        }).start();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            onBackPressed();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
}

package deadpixel.app.vapor.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import deadpixel.app.vapor.ui.AccountActivity;
import deadpixel.app.vapor.R;
import deadpixel.app.vapor.callbacks.AccountStatsUpdateEvent;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.okcloudapp.model.AccountModel;
import deadpixel.app.vapor.okcloudapp.model.AccountStatsModel;
import deadpixel.app.vapor.database.DatabaseManager;
import deadpixel.app.vapor.utils.AppUtils;


public class AccountMainFragment  extends Fragment {
    private final String TAG = "AccountMainFragment";

    private ViewGroup mViewGroup;
    private TextView title;
    private TextView summary;

    private boolean isLoading = false;

    private AnimationDrawable refreshIcon;
    private MenuItem refreshMenuItem;
    private Map<String, View> views;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.account_fragment, container, false);

        setRetainInstance(true);

        if(getActivity() != null) {
            if(getActivity().getActionBar() != null) {
                getActivity().getActionBar().setTitle(R.string.account_activity_title);
            }
        }

        setUpFragment(v);

        try {
            AppUtils.addToRequestQueue(AppUtils.api.requestAccountDetails());
        } catch (CloudAppException e) {
            Log.e(TAG, "Error requesting account details");
        }

        return v;
    }

    @Override
    public void onDestroyView() {
        Crouton.cancelAllCroutons();
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        AppUtils.getEventBus().register(ServerEventHandler);
        Log.i(TAG, "Registering " + TAG + "to EventBus.."  );
    }

    @Override
    public void onDetach() {
        AppUtils.getEventBus().unregister(ServerEventHandler);
        Log.i(TAG, "Unregistering " + TAG + "from EventBus.."  );
        super.onDetach();
    }


    private void setUpFragment(View v) {

        views = setUpListeners(adjustViews(v));

    }

    private void refreshViews()  {
        AccountModel account = (AccountModel) DatabaseManager.getCloudAppAccount();
        AccountStatsModel accountStats = (AccountStatsModel) DatabaseManager.getCloudAppAccountStats();

        setActionBarTitle(account.getEmail());

        setAccountType(account.isSubscribed());

        try {
            setSubscriptionEnd(account.getSubscriptionExpiresAt());
        } catch (CloudAppException e) {
            Log.e(TAG, "Error getting subscription expiration date");
        }

        setPrivateLinks(account.isPrivateItems());


        if(accountStats != null) {

            //if account is fully synced the app will be able to get a more accurate
            //total of items in account.
            boolean fullySynced = AppUtils.mPref.getBoolean(AppUtils.FULLY_SYNCED, false);
            long totalItems = accountStats.getItems();
            long calculatedTotal = AppUtils.mPref.getLong(AppUtils.CALCULATED_TOTAL_ITEMS, totalItems);

            if(fullySynced) {
               setTotalItems(String.valueOf(calculatedTotal));
            } else {
                setTotalItems(String.valueOf(accountStats.getItems()));
            }

            setTotalViews(String.valueOf(accountStats.getViews()));
        }


        try {
            setMemberSince(account.getFormattedCreatedAt());
        } catch (CloudAppException e) {
            Log.e(TAG, "Error getting created_at date");
        }

        views.get(AppUtils.AM_PRIVATE_LINKS).findViewById(R.id.switchWidget).setEnabled(true);


    }

    private Map<String, View> adjustViews(View v) {
        Map<String, View> adjustedViews = new HashMap<String, View>();

        View tempView;

        //Account Type
        tempView = v.findViewById(R.id.layout_account_type);
        tempView.setBackgroundResource(R.drawable.preference_divided_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.account_type);
        summary.setText("Unknown");
        adjustedViews.put(AppUtils.AM_ACCOUNT_TYPE, tempView);

        //Subscriptions Ends At
        tempView = v.findViewById(R.id.layout_subscription);
        tempView.setBackgroundResource(R.drawable.preference_divided_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.subscription_ends_at);
        summary.setText("Unknown");
        adjustedViews.put(AppUtils.AM_SUBSCRIPTION_END, tempView);

        //Custom Domain
        tempView = v.findViewById(R.id.layout_custom_domain);
        tempView.setBackgroundResource(R.drawable.preference_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.custom_domain);
        summary.setVisibility(View.GONE);
        adjustedViews.put(AppUtils.AM_CUSTOM_DOMAIN, tempView);

        //Management
        tempView = v.findViewById(R.id.layout_management_header);
        title = (TextView) tempView.findViewById(R.id.header);
        title.setText(R.string.header_account_management);
        adjustedViews.put(AppUtils.AM_MANAGEMENT, tempView);

        //Change Email
        tempView = v.findViewById(R.id.layout_change_email);
        tempView.setBackgroundResource(R.drawable.preference_divided_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.change_email);
        summary.setVisibility(View.GONE);
        adjustedViews.put(AppUtils.AM_CHANGE_EMAIL, tempView);

        //Change Password
        tempView = v.findViewById(R.id.layout_change_password);
        tempView.setBackgroundResource(R.drawable.preference_divided_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.change_password);
        summary.setVisibility(View.GONE);
        adjustedViews.put(AppUtils.AM_CHANGE_PASSWORD, tempView);

        //Private Links
        tempView = v.findViewById(R.id.layout_private_links);
        tempView.setBackgroundResource(R.drawable.preference_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.private_links);
        summary.setText(R.string.private_links_summary);
        adjustedViews.put(AppUtils.AM_PRIVATE_LINKS, tempView);

        //Statistics
        tempView = v.findViewById(R.id.layout_statistics_header);
        title = (TextView) tempView.findViewById(R.id.header);
        title.setText(R.string.header_statistics);
        adjustedViews.put(AppUtils.AM_STATISTICS, tempView);

        //Total Views
        tempView = v.findViewById(R.id.layout_total_views);
        tempView.setBackgroundResource(R.drawable.preference_divided_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.total_views);
        summary.setText("Unknown");
        adjustedViews.put(AppUtils.AM_TOTAL_VIEWS, tempView);

        //Total Items
        tempView = v.findViewById(R.id.layout_total_items);
        tempView.setBackgroundResource(R.drawable.preference_divided_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.total_items);
        summary.setText("Unknown");
        adjustedViews.put(AppUtils.AM_TOTAL_ITEMS, tempView);

        //Uploads Today
        tempView = v.findViewById(R.id.layout_uploads_today);
        tempView.setBackgroundResource(R.drawable.preference_divided_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.uploads_today);
        summary.setText("Unknown");
        adjustedViews.put(AppUtils.AM_UPLOADS_TODAY, tempView);

        //Member Since
        tempView = v.findViewById(R.id.layout_member_since);
        tempView.setBackgroundResource(R.drawable.preference_divided_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.member_since);
        summary.setText("Unknown");
        adjustedViews.put(AppUtils.AM_MEMBER_SINCE, tempView);



        //so that refresh views can work in and out of the scope of this method
        views = adjustedViews;

        //Must be called before any listeners (specifically the switch listener) are set.
        //This manually sets the default values of the views.
        //The listeners listen for user changes
        refreshViews();

        
        return adjustedViews;
    }

    private Map<String, View> setUpListeners(Map<String, View> views) {

        //Custom Domain
        views.get(AppUtils.AM_CUSTOM_DOMAIN)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Crouton.cancelAllCroutons();
                        ((AccountActivity)getActivity()).performTransaction(new CustomDomainFragment());
                    }
                });

        //Change Email
        views.get(AppUtils.AM_CHANGE_EMAIL)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Crouton.cancelAllCroutons();
                        ((AccountActivity)getActivity()).performTransaction(new ChangeEmailFragment());
                    }
                });

        //Change Password
        views.get(AppUtils.AM_CHANGE_PASSWORD)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Crouton.cancelAllCroutons();
                        ((AccountActivity)getActivity()).performTransaction(new ChangePasswordFragment());
                    }
                });



        //Private Links
        final CompoundButton v = (CompoundButton) views.get(AppUtils.AM_PRIVATE_LINKS).findViewById(R.id.switchWidget);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                if (v.isChecked()) {
                    try {
                        buttonView.setEnabled(false);
                        refreshIcon.start();
                        AppUtils.addToRequestQueue(AppUtils.api.setDefaultSecurity(CloudAppAccount.DefaultSecurity.PRIVATE));
                    } catch (CloudAppException e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else {
                    try {
                        buttonView.setEnabled(false);
                        refreshIcon.start();
                        AppUtils.addToRequestQueue(AppUtils.api.setDefaultSecurity(CloudAppAccount.DefaultSecurity.PUBLIC));
                    } catch (CloudAppException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });

        return views;
    }

    private void setActionBarTitle(String email) {

        if(getActivity() != null) {
            if (getActivity().getActionBar() != null) {
                getActivity().getActionBar().setSubtitle(email);
            }
        }

    }

    private void setAccountType(boolean isSubscribed) {

        summary = (TextView) views.get(AppUtils.AM_ACCOUNT_TYPE)
                .findViewById(R.id.summary);
        if(isSubscribed) {
            String ACCOUNT_TYPE_PAID = "Pro";
            summary.setText(ACCOUNT_TYPE_PAID);

            View v = views.get(AppUtils.AM_CUSTOM_DOMAIN);
                    v.setEnabled(true);



            //views.get(AppUtils.AM_SUBSCRIPTION_END)
                   //.setEnabled(true);
        } else {

            String ACCOUNT_TYPE_FREE = "Free";
            summary.setText(ACCOUNT_TYPE_FREE);

            View v = views.get(AppUtils.AM_CUSTOM_DOMAIN)
                   ;

            disableView(v);

            //views.get(AppUtils.AM_SUBSCRIPTION_END)
                    //.setEnabled(false);
        }

    }

    private void disableView(View v){

        TextView title  = (TextView) v.findViewById(R.id.title);
        TextView summary = (TextView) v.findViewById(R.id.summary);

        v.setEnabled(false);
        if(title != null) {
            title.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));

        }

        if(summary != null) {
            summary.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
        }
    }

    private void setSubscriptionEnd(String subscriptionEnd) {

        View v =  views.get(AppUtils.AM_SUBSCRIPTION_END);

        title = (TextView) v.findViewById(R.id.title);
        summary = (TextView) v.findViewById(R.id.summary);


        if (subscriptionEnd == null) {
            title.setText("Not Subscribed");
            summary.setText("Click here to view plans");
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "http://my.cl.ly/plans";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });
        } else {
            summary.setText(subscriptionEnd);
        }

    }

    private void setPrivateLinks(boolean bool) {
        CompoundButton v = (CompoundButton) views.get(AppUtils.AM_PRIVATE_LINKS).findViewById(R.id.switchWidget);

        v.setChecked(bool);

            title = (TextView) views.get(AppUtils.AM_PRIVATE_LINKS)
                .findViewById(R.id.title);
            summary = (TextView) views.get(AppUtils.AM_PRIVATE_LINKS)
                .findViewById(R.id.summary);
    }

    private void setTotalItems(String totalItems) {
        summary = (TextView) views.get(AppUtils.AM_TOTAL_ITEMS)
                .findViewById(R.id.summary);
        summary.setText(totalItems);
    }

    private void setTotalViews(String totalViews) {
        summary = (TextView) views.get(AppUtils.AM_TOTAL_VIEWS)
                .findViewById(R.id.summary);
        summary.setText(totalViews);
    }

    private void setUploadsToday(String uploadsToday) {
        summary = (TextView) views.get(AppUtils.AM_UPLOADS_TODAY)
                .findViewById(R.id.summary);
        summary.setText(uploadsToday);
    }
    private void setMemberSince(String memberSince) {
        summary = (TextView) views.get(AppUtils.AM_MEMBER_SINCE)
                .findViewById(R.id.summary);
        if (memberSince == null) {
            summary.setText("Loading...");
        } else {
            summary.setText(memberSince);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        refreshMenuItem = menu.findItem(R.id.account_refresh);
        refreshMenuItem.setIcon(R.drawable.cloud_refresh);
        refreshIcon = (AnimationDrawable) refreshMenuItem.getIcon();
        super.onPrepareOptionsMenu(menu);
    }

    public void setOkIcon() {
        refreshMenuItem.setIcon(R.drawable.cloud_ok);
        refreshIcon = (AnimationDrawable) refreshMenuItem.getIcon();
        refreshIcon.setOneShot(true);

        int duration = 0;

        for(int i = 0; i <= refreshIcon.getNumberOfFrames() - 1; i++ ) {
            duration += refreshIcon.getDuration(i);
        }

        refreshIcon.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Don't want this being called if user has already left the fragment
                if(isAdded()) {
                    //calls onPrepareOptions menu. Effectively resetting the actionbar.
                    getActivity().supportInvalidateOptionsMenu();
                    isLoading = false;
                }
            }
        }, duration);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.account_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();

        if(id == R.id.account_refresh && !isLoading) {
            try {
                isLoading = true;
                refreshIcon.start();
                AppUtils.addToRequestQueue(AppUtils.api.requestAccountDetails());
            } catch (CloudAppException e) {
                Log.e(TAG, "Error requesting account details");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

    }

    private void toggleSwitch() {
        CompoundButton v = (CompoundButton) views.get(AppUtils.AM_PRIVATE_LINKS).findViewById(R.id.switchWidget);
        if (v.isChecked())
            v.setChecked(false);
        else {
            v.setChecked(true);
        }
    }

    private void stopRefreshIcon() {
        if(refreshIcon.isRunning())
            refreshIcon.stop();
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

    public Object ServerEventHandler = new Object() {

        @Subscribe
        public void onAccountStatsUpdate(AccountStatsUpdateEvent event) {
            isLoading = false;
            if(!isDetached() || !isRemoving()) {
                stopRefreshIcon();

                setOkIcon();

                toggleSwitch();

                refreshViews();

                //AppUtils.makeCrouton(getActivity(), getResources().getString(R.string.account_details_updated), AppUtils.Style.INFO);

            }
        }

        @Subscribe
        public void onErrorEvent(ErrorEvent errorEvent) {

            isLoading = false;
            if(!isDetached() || !isRemoving()) {
                AppUtils.makeCrouton(getActivity(), getErrorDescription(errorEvent), AppUtils.Style.ALERT);

                views.get(AppUtils.AM_PRIVATE_LINKS).findViewById(R.id.switchWidget).setEnabled(true);

                stopRefreshIcon();

                refreshViews();
            }

        }
    };

}

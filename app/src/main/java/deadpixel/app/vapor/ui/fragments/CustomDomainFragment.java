package deadpixel.app.vapor.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.otto.Subscribe;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import deadpixel.app.vapor.R;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.callbacks.ResponseEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.impl.model.AccountModel;
import deadpixel.app.vapor.libs.TransitionButton;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 7/7/2014.
 */
public class CustomDomainFragment extends Fragment {

    private final String TAG = "CustomDomainFragment";
    private State mState;
    TextView title;
    TextView summary;

    private boolean isListening = false;
    private EditText mCustomDomain;
    private EditText mCustomDomainHomepage;
    private TransitionButton mButton;

    Map<String,View> views;

    View v;

    Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isListening = false;

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.custom_domain_fragment, container, false);

        getActivity().getActionBar().setTitle(R.string.custom_domain);
        getActivity().getActionBar().setSubtitle(null);

        setUpFragment(v);

        updateState(State.NORMAL);
        return v;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {

        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpFragment(View v) {
        views = setUpListeners(adjustViews(v));
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
        Log.i(TAG, "Unregistering " + TAG + "from EventBus..");
        super.onDetach();
    }

    private Map<String, View> setUpListeners(Map<String, View> views) {
        //TODO No implementation currently for listeners in Custom Domain Fragment
        Button btn = (Button) views.get(AppUtils.CD_BTN_UPDATE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if(isValidFields()) {
                try {
                    isListening = true;
                    AppUtils.getEventBus().register(ServerEventHandler);
                    AppUtils.addToRequestQueue(AppUtils.api.setCustomDomain(mCustomDomain.getText()
                            .toString(), mCustomDomainHomepage.getText().toString()));
                } catch (CloudAppException e) {
                    e.printStackTrace();
                }
            }

            }
        });
        return views;
    }

    private boolean isValidFields() {
        if (AppUtils.isEmpty(mCustomDomain.getText().toString())) {
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_custom_domain))
                    .start();
            YoYo.with(Techniques.Shake).duration(1500).playOn(mCustomDomain);
            updateState(State.ERROR);
            return false;
        }
        else if (!AppUtils.isValidWebAddress(mCustomDomain.getText().toString())) {
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.invalid_address))
                    .start();
            YoYo.with(Techniques.Shake).duration(1500).playOn(mCustomDomain);
            updateState(State.ERROR);
            return false;
        }
        else if (AppUtils.isEmpty(mCustomDomainHomepage.getText().toString())) {
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_custom_domain_homepage))
                    .start();
            YoYo.with(Techniques.Shake).duration(1500).playOn(mCustomDomain);
            updateState(State.ERROR);
            return false;
        } else if(!AppUtils.isValidWebAddress(mCustomDomainHomepage.getText().toString())) {
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.invalid_address))
                    .start();
            YoYo.with(Techniques.Shake).duration(1500).playOn(mCustomDomainHomepage);
            updateState(State.ERROR);
            return false;
        } else {
            updateState(State.NORMAL);
            return true;
        }

    }

    public Map<String,View> adjustViews(View v) {

        Map<String ,View> adjustedViews = new HashMap<String, View>();

        View tempView;

        //Custom Domain
        tempView = v.findViewById(R.id.layout_pref_custom_domain);
        tempView.setBackgroundResource(R.drawable.preference_divided_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.custom_domain);
        summary.setText("Unknown");
        adjustedViews.put(AppUtils.CD_CUSTOM_DOMAIN, tempView);

        //Custom Domain Homepage
        tempView = v.findViewById(R.id.layout_pref_custom_homepage);
        tempView.setBackgroundResource(R.drawable.preference_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.custom_domain_homepage);
        summary.setText("Unknown");
        adjustedViews.put(AppUtils.CD_CUSTOM_DOMAIN_HOMEPAGE, tempView);

        //Domain Field
        tempView = v.findViewById(R.id.custom_domain_field);
        mCustomDomain = (EditText)tempView;
        adjustedViews.put(AppUtils.CD_FIELD_CUSTOM_DOMAIN_FIELD, tempView);

        //Domain Homepage Field
        tempView = v.findViewById(R.id.custom_domain_homepage_field);
        mCustomDomainHomepage = (EditText)tempView;
        adjustedViews.put(AppUtils.CD_CUSTOM_DOMAIN_HOMEPAGE_FIELD, tempView);

        //button
        tempView = v.findViewById(R.id.update_domain);
        mButton = (TransitionButton)tempView;
        mButton.from(getActivity())
                .setNormalSelector(R.drawable.btn_toggle_selector)
                .setErrorSelector(R.drawable.btn_toggle_error_selector)
                .setSuccessSelector(R.drawable.btn_toggle_success_selector);
        adjustedViews.put(AppUtils.CD_BTN_UPDATE, tempView);

        //so that refresh views can work in and out of the scope of this method
        views = adjustedViews;

        //Must be called before any listeners (specifically the switch listener) are set.
        //This manually sets the default values of the views.
        //The listeners listen for user changes
        refreshViews();

        return  adjustedViews;

    }

    public void setCustomDomain(String customDomain) {
        View v = views.get(AppUtils.CD_CUSTOM_DOMAIN);
        summary = (TextView) v.findViewById(R.id.summary);
        summary.setText(customDomain);
    }

    public void setCustomDomainHomepage(String customHomepage) {
        View v = views.get(AppUtils.CD_CUSTOM_DOMAIN_HOMEPAGE);
        summary = (TextView) v.findViewById(R.id.summary);
        summary.setText(customHomepage);
    }

    public void refreshViews() {
        AccountModel account;
        try {
            account = (AccountModel) AppUtils.api.getAccountDetails();

            setCustomDomain(account.getDomain());

            setCustomDomainHomepage(account.getDomainHomePage());

        } catch (CloudAppException e) {
            Log.e(TAG, "Error getting account details");
        }


    }

    //Takes a guess as to what error type is given the particular
    // state of the application, sign in, register or forgot password, though not
    //implemented since it's not needed in this case

    private enum State {
        NORMAL, PROGRESS, ERROR, SUCCESS
    }

    private void updateState(State state) {
        switch(state) {
            case NORMAL:
                mCustomDomain.setVisibility(View.VISIBLE);
                mCustomDomain.setEnabled(true);
                mCustomDomainHomepage.setVisibility(View.VISIBLE);
                mCustomDomainHomepage.setEnabled(true);
                mButton.setEnabled(true);
                mButton.setText(R.string.update_domain);
                break;
            case PROGRESS:
                mCustomDomain.setVisibility(View.VISIBLE);
                mCustomDomain.setEnabled(false);
                mCustomDomainHomepage.setVisibility(View.VISIBLE);
                mCustomDomainHomepage.setEnabled(false);
                mButton.setEnabled(false);
                mButton.setText(R.string.updating);
            case ERROR:
                mCustomDomain.setVisibility(View.VISIBLE);
                mCustomDomain.setEnabled(true);
                mCustomDomainHomepage.setVisibility(View.VISIBLE);
                mCustomDomainHomepage.setEnabled(true);
                mButton.setEnabled(true);
                break;
            case SUCCESS:
                mCustomDomain.setVisibility(View.VISIBLE);
                mCustomDomain.setText("");
                mCustomDomain.setEnabled(true);
                mCustomDomainHomepage.setVisibility(View.VISIBLE);
                mCustomDomain.setText("");
                mCustomDomainHomepage.setEnabled(true);
                mButton.setEnabled(true);
                break;

            default:
                break;

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
                        errorDescription = ((Integer) HttpStatus.SC_UNPROCESSABLE_ENTITY).toString();
                        break;
                    case HttpStatus.SC_UNAUTHORIZED:
                        errorDescription = ((Integer) HttpStatus.SC_UNAUTHORIZED).toString();
                        break;
                    case HttpStatus.SC_NOT_ACCEPTABLE:
                        errorDescription = ((Integer) HttpStatus.SC_NOT_ACCEPTABLE).toString();
                        break;
                    case HttpStatus.SC_NO_CONTENT:
                        errorDescription = "That email belongs to another user - 204";
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
     * Otto has a limitation (as per design) that it will only find
     * methods on the immediate class type. As a result, if at runtime this instance
     * actually points to a subclass implementation, the methods registered in this class will
     * not be found. This immediately becomes a problem when using the AndroidAnnotations
     * framework as it always produces a subclass of annotated classes.
     *
     * To get around the class hierarchy limitation, one can use a separate anonymous class to
     * handle the events.
     */
    private Object ServerEventHandler = new Object() {

        @Subscribe
        public void onServerResponse(ResponseEvent response) {

            if(!isDetached() || !isRemoving()) {

                updateState(State.SUCCESS);

                if(isListening) {
                    isListening = false;
                    mButton.setType(TransitionButton.BtnType.SUCCESS)
                            .setFromText(getResources().getString(R.string.update_domain))
                            .setToText(getResources().getString(R.string.account_details_updated))
                            .setDuration(4000)
                            .start();
                }

                refreshViews();

            }
        }

        //Subscribed to receive ErrorEvents
        @Subscribe
        public void onServerError(ErrorEvent errorEvent) {

            if(!isDetached() || !isRemoving()) {
                updateState(State.ERROR);

                String errorDescription = getErrorDescription(errorEvent);

                //Checks to see if there is an internet connection
                //This is to let the Volley API determine if it cannot establish a connection
                if (!errorEvent.getErrorDescription().equals(AppUtils.NO_CONNECTION)) {

                    mButton.setType(TransitionButton.BtnType.ERROR)
                            .setFromText(getResources().getString(R.string.update_domain))
                            .setToText(errorDescription)
                            .setDuration(4000)
                            .start();

                    YoYo.with(Techniques.Shake).duration(1500).playOn(mCustomDomain);
                    YoYo.with(Techniques.Shake).duration(1500).playOn(mCustomDomainHomepage);

                } else {

                    mButton.setType(TransitionButton.BtnType.ERROR)
                            .setFromText(getResources().getString(R.string.update_domain))
                            .setToText(getResources().getString(R.string.check_internet))
                            .setDuration(4000)
                            .start();
                }

            }
        }

    };


}
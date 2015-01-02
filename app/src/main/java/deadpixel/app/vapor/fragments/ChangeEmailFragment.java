package deadpixel.app.vapor.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.VolleyError;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.otto.Subscribe;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import deadpixel.app.vapor.R;
import deadpixel.app.vapor.callbacks.AccountUpdateEvent;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.libs.TransitionButton;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 7/7/2014.
 */
public class ChangeEmailFragment extends SherlockFragment{

    private static String TAG = "ChangeEmailFragment";
    private static State mState;
    private static TextView title;
    private static TextView summary;

    private boolean isListening =false;
    private static EditText mEmailField;
    private static EditText mPasswordField;
    private static TransitionButton mButton;

    Map<String,View> views;

    View v;

    Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.change_email_fragment, container, false);

        getActivity().getActionBar().setTitle(R.string.change_email);
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
        Log.i(TAG, "Unregistering " + TAG + "from EventBus.."  );
        super.onDetach();
    }


    private Map<String, View> setUpListeners(Map<String, View> views) {
        //TODO No implementation currently for listeners in Custom Domain Fragment
        final Button btn = (Button) views.get(AppUtils.CE_BTN_UPDATE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isValidFields()) {
                    try {
                        updateState(State.PROGRESS);
                        isListening = true;
                        Log.i(TAG, "Setting email: " + "Email: "+ mEmailField.getText().toString() + " " + "Password: " + mPasswordField.getText().toString());
                        AppUtils.api.setEmail(mEmailField.getText().toString(), mPasswordField.getText().toString());

                    } catch (CloudAppException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return views;
    }

    private boolean isValidFields() {
        if (AppUtils.isEmpty(mEmailField.getText().toString())) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mEmailField);
            updateState(State.ERROR);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_email))
                    .start();

            //AppUtils.makeCrouton(getActivity(), getResources().getString(R.string.empty_email), Style.INFO);
            Log.i(TAG, "Email field is empty" );
            return false;
        } else if (!AppUtils.isValidEmail(mEmailField.getText().toString())) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mEmailField);
            updateState(State.ERROR);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.invalid_email))
                    .start();
            //AppUtils.makeCrouton(getActivity(), getResources().getString(R.string.invalid_email), Style.INFO);
            Log.i(TAG, "Invalid email format: " + mEmailField.getText().toString() );
            return false;
        } else if(isEmailMatchWithPrefs(mEmailField)) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mEmailField);
            updateState(State.ERROR);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.new_email_same))
                    .start();
            //AppUtils.makeCrouton(getActivity(), getResources().getString(R.string.new_email_same), Style.INFO);
            Log.i(TAG, "Email is match with preferences: " + mEmailField.getText().toString());
            return false;
        } else if(AppUtils.isEmpty(mPasswordField.getText().toString())) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mPasswordField);
            updateState(State.ERROR);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_password))
                    .start();
            //AppUtils.makeCrouton(getActivity(), getResources().getString(R.string.empty_password), Style.INFO);
            Log.i(TAG, "Password is empty" );
            return false;
        } else if (!isPassMatchWithPrefs(mPasswordField)){
            YoYo.with(Techniques.Shake).duration(1500).playOn(mPasswordField);
            updateState(State.ERROR);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.incorrect_pass))
                    .start();
            //AppUtils.makeCrouton(getActivity(), getResources().getString(R.string.incorrect_pass), Style.INFO);
            Log.i(TAG, "Password does not match with pref: " + mPasswordField.getText().toString() );
            return false;
    } else {
        updateState(State.NORMAL);
        return true;
    }

    }

    private boolean isEmailMatchWithPrefs(EditText emailField) {

        String email = AppUtils.getEmail();

        return emailField.getText().toString().equals(email);
    }

    private boolean isPassMatchWithPrefs(EditText passField) {
        String pass =  AppUtils.getPass();

        return passField.getText().toString().equals(pass);
    }


    public Map<String ,View> adjustViews(View v) {

        Map<String,View> adjustedViews = new HashMap<String, View>();

        View tempView;

        //Current Email
        tempView = v.findViewById(R.id.layout_pref_current_email);
        tempView.setBackgroundResource(R.drawable.preference_item_selector);
        title = (TextView) tempView.findViewById(R.id.title);
        summary = (TextView) tempView.findViewById(R.id.summary);
        title.setText(R.string.current_email);
        summary.setText("Unknown");
        adjustedViews.put(AppUtils.CE_CURRENT_EMAIL, tempView);

        //Email field
        tempView = v.findViewById(R.id.ce_new_email_field);
        mEmailField = (EditText)tempView;
        adjustedViews.put(AppUtils.CE_EMAIL_FIELD, tempView);

        //Password field
        tempView = v.findViewById(R.id.ce_pass_field);
        mPasswordField = (EditText)tempView;
        adjustedViews.put(AppUtils.CE_PASS_FIELD, tempView);

        //button
        tempView = v.findViewById(R.id.ce_btn_change_email);
        mButton = (TransitionButton)tempView;
        mButton.from(getActivity())
                .setNormalSelector(R.drawable.btn_toggle_selector)
                .setErrorSelector(R.drawable.btn_toggle_error_selector)
                .setSuccessSelector(R.drawable.btn_toggle_success_selector);
        adjustedViews.put(AppUtils.CE_BTN_UPDATE, tempView);


        //so that refresh views can work in and out of the scope of this method
        views = adjustedViews;

        //Must be called before any listeners (specifically the switch listener) are set.
        //This manually sets the default values of the views.
        //The listeners listen for user changes
        refreshViews();

        return  adjustedViews;

    }

    private void refreshViews() {

            setCurrentEmail(AppUtils.getEmail());

    }

    public void setCurrentEmail(String currentEmail) {
        View v = views.get(AppUtils.CE_CURRENT_EMAIL);
        summary = (TextView) v.findViewById(R.id.summary);
        summary.setText(currentEmail);
    }

    private enum State {
        NORMAL, PROGRESS, ERROR, SUCCESS
    }

    private static void updateState(State state) {
        switch(state) {
            case NORMAL:
                mEmailField.setVisibility(View.VISIBLE);
                mEmailField.setEnabled(true);
                mPasswordField.setVisibility(View.VISIBLE);
                mPasswordField.setEnabled(true);
                mButton.setEnabled(true);
                mButton.setText(R.string.update);
                break;
            case PROGRESS:
                mEmailField.setVisibility(View.VISIBLE);
                mEmailField.setEnabled(false);
                mPasswordField.setVisibility(View.VISIBLE);
                mPasswordField.setEnabled(false);
                mButton.setText(R.string.updating);
                mButton.setEnabled(false);
                break;
            case ERROR:
                mEmailField.setVisibility(View.VISIBLE);
                mEmailField.setEnabled(true);
                mPasswordField.setVisibility(View.VISIBLE);
                mPasswordField.setEnabled(true);
                mButton.setEnabled(true);


                break;
            case SUCCESS:
                mEmailField.setVisibility(View.VISIBLE);
                mEmailField.setText("");
                mEmailField.setEnabled(true);
                mPasswordField.setVisibility(View.VISIBLE);
                mPasswordField.setText("");
                mPasswordField.setEnabled(true);
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
        String errorDescription = ErrorEvent.getErrorDescription(error);
        VolleyError volleyError;

        if(error.getError() instanceof  VolleyError) {
            volleyError = (VolleyError) error.getError();

            if (volleyError.networkResponse == null) {
                errorDescription = getResources().getString(R.string.error_contacting_cloudapp);
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
                        errorDescription = "That email belongs to another user";
                        break;
                    default:
                        break;
                }
            }
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


    public Object ServerEventHandler = new Object() {

        @Subscribe
        public void onServerResponse(AccountUpdateEvent response) {

            if(!isDetached() || !isRemoving()) {
                updateState(State.SUCCESS);


                if(isListening) {
                    isListening = true;
                    mButton.setType(TransitionButton.BtnType.SUCCESS)
                            .setFromText(getResources().getString(R.string.update))
                            .setToText(getResources().getString(R.string.account_details_updated))
                            .setDuration(4000)
                            .start();
                }

                refreshViews();
            }


            AppUtils.setAuth();
        }

        //Subscribed to receive ErrorEvents
        @Subscribe
        public void onErrorEvent(ErrorEvent errorEvent) {
            updateState(State.ERROR);

            String errorDescription = getErrorDescription(errorEvent);

            //Checks to see if there is an internet connection
            //This is to let the Volley API determine if it cannot establish a connection
            if(!errorEvent.getExplicitError().equals(AppUtils.NO_CONNECTION)) {

                mButton.setType(TransitionButton.BtnType.ERROR)
                        .setFromText(getResources().getString(R.string.update))
                        .setToText(errorDescription)
                        .setDuration(4000)
                        .start();

                YoYo.with(Techniques.Shake).duration(1500).playOn(mEmailField);
                YoYo.with(Techniques.Shake).duration(1500).playOn(mPasswordField);

            } else {
                mButton.setType(TransitionButton.BtnType.ERROR)
                        .setFromText(getResources().getString(R.string.update))
                        .setToText(errorDescription)
                        .setDuration(4000)
                        .start();
            }
        }
    };


}

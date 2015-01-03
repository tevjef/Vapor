package deadpixel.app.vapor.ui.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import deadpixel.app.vapor.callbacks.AccountUpdateEvent;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.impl.model.AccountModel;
import deadpixel.app.vapor.libs.TransitionButton;
import deadpixel.app.vapor.utils.AppUtils;

public class ChangePasswordFragment extends Fragment {

    private static String TAG = "ChangeEmailFragment";
    private static State mState;
    private static TextView title;
    private static TextView summary;

    private boolean isListening = false;
    private static EditText mCurrentPassField;
    private static EditText mNewPassField;
    private static EditText mConfirmNewPassField;
    private static TransitionButton mButton;

    Map<String,View> views;

    View v;

    Activity activity;


    SharedPreferences mPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getActionBar().setTitle(R.string.change_password);
        getActivity().getActionBar().setSubtitle(null);

        setHasOptionsMenu(true);

        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.change_password_fragment, container, false);

        setUpFragment(v);

        updateState(State.NORMAL);
        return v;

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {

        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpFragment(View v) {
        views = setUpListeners(adjustViews(v));
    }



    private Map<String, View> setUpListeners(Map<String, View> views) {
        //TODO No implementation currently for listeners in Custom Domain Fragment
        final Button btn = (Button) views.get(AppUtils.CP_BTN_UPDATE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isValidFields()) {
                    try {
                        isListening = true;
                        Log.d(TAG, "Resetting Password: " + "Current Password: "+ mCurrentPassField.getText().toString()
                                + " " + "New Password: " + mNewPassField.getText().toString());
                        updateState(State.PROGRESS);
                        AppUtils.addToRequestQueue(AppUtils.api.setPassword(mNewPassField.getText().toString(), mCurrentPassField.getText().toString()));
                    } catch (CloudAppException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return views;
    }

    private boolean isValidFields() {
        if (AppUtils.isEmpty(mCurrentPassField.getText().toString())) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mCurrentPassField);
            updateState(State.ERROR);

            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_password))
                    .start();

            Log.d(TAG, getResources().getString(R.string.empty_password));
            return false;
        } else if (!isPassMatchWithPrefs(mCurrentPassField)) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mCurrentPassField);
            updateState(State.ERROR);

            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.incorrect_pass))
                    .start();

            Log.d(TAG, "Password doesn't match with what was saved in preferences");
            return false;
        } else if (AppUtils.isEmpty(mNewPassField.getText().toString())) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mNewPassField);
            updateState(State.ERROR);

            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_password))
                    .start();

            Log.d(TAG, getResources().getString(R.string.empty_password));
            return false;
        } else if (isPassMatchWithPrefs(mNewPassField)) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mNewPassField);
            updateState(State.ERROR);

            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.invalid_new_pass_same_as_old))
                    .start();

            Log.d(TAG, "New password matches with what was saved in preferences");
            return false;
        }else if (AppUtils.isEmpty(mConfirmNewPassField.getText().toString())) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mConfirmNewPassField);
            updateState(State.ERROR);

            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_password))
                    .start();

            Log.d(TAG, getResources().getString(R.string.empty_password));
            return false;
        } else if (!mNewPassField.getText().toString().equals(mConfirmNewPassField.getText().toString())) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mNewPassField);
            YoYo.with(Techniques.Shake).duration(1500).playOn(mConfirmNewPassField);
            updateState(State.ERROR);

            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.invalid_pass_no_match))
                    .start();

            Log.d(TAG, getResources().getString(R.string.invalid_pass_no_match) + " : " + mConfirmNewPassField.getText().toString());
            return false;

        } else {
            updateState(State.NORMAL);
            return true;
        }

    }


    private boolean isPassMatchWithPrefs(EditText passField) {

        String pass =  AppUtils.getPass();

        return passField.getText().toString().equals(pass);
    }


    public Map<String ,View> adjustViews(View v) {

        Map<String,View> adjustedViews = new HashMap<String, View>();

        View tempView;

        //Current password field
        tempView = v.findViewById(R.id.cp_current_pass_field);
        mCurrentPassField = (EditText)tempView;
        adjustedViews.put(AppUtils.CP_CURRENT_PASSWORD, tempView);

        //New password field
        tempView = v.findViewById(R.id.cp_new_pass_field);
        mNewPassField = (EditText)tempView;
        adjustedViews.put(AppUtils.CP_NEW_PASSWORD_FIELD, tempView);

        //Confirm new password field
        tempView = v.findViewById(R.id.cp_confirm_new_pass_field);
        mConfirmNewPassField = (EditText)tempView;
        adjustedViews.put(AppUtils.CP_CONFIRM_NEW_PASSWORD_FIELD, tempView);

        //button
        tempView = v.findViewById(R.id.cp_btn_change_password);
        mButton = (TransitionButton)tempView;
        mButton.from(getActivity())
                .setNormalSelector(R.drawable.btn_toggle_selector)
                .setErrorSelector(R.drawable.btn_toggle_error_selector)
                .setSuccessSelector(R.drawable.btn_toggle_success_selector);
        adjustedViews.put(AppUtils.CP_BTN_UPDATE, tempView);


        //so that refresh views can work in and out of the scope of this method
        views = adjustedViews;

        //Must be called before any listeners (specifically the switch listener) are set.
        //This manually sets the default values of the views.
        //The listeners listen for user changes
        refreshViews();

        return  adjustedViews;

    }

    private void refreshViews() {
        AccountModel account;
        try {
            account = (AccountModel) AppUtils.api.getAccountDetails();

        } catch (CloudAppException e) {
            Log.e(TAG, "Error getting account details");
        }
    }




    private enum State {
        NORMAL, PROGRESS, ERROR, SUCCESS
    }

    private static void updateState(State state) {
        switch(state) {
            case NORMAL:
                mCurrentPassField.setVisibility(View.VISIBLE);
                mCurrentPassField.setEnabled(true);
                mNewPassField.setVisibility(View.VISIBLE);
                mNewPassField.setEnabled(true);
                mConfirmNewPassField.setVisibility(View.VISIBLE);
                mConfirmNewPassField.setEnabled(true);
                mButton.setEnabled(true);
                mButton.setText(R.string.update);
                break;
            case PROGRESS:
                mCurrentPassField.setVisibility(View.VISIBLE);
                mCurrentPassField.setEnabled(false);
                mNewPassField.setVisibility(View.VISIBLE);
                mNewPassField.setEnabled(false);
                mConfirmNewPassField.setVisibility(View.VISIBLE);
                mConfirmNewPassField.setEnabled(false);
                mButton.setEnabled(false);
                mButton.setText(R.string.updating);
            case ERROR:
                mCurrentPassField.setVisibility(View.VISIBLE);
                mCurrentPassField.setEnabled(true);
                mNewPassField.setVisibility(View.VISIBLE);
                mNewPassField.setEnabled(true);
                mConfirmNewPassField.setVisibility(View.VISIBLE);
                mConfirmNewPassField.setEnabled(true);
                mButton.setEnabled(false);
                break;
            case SUCCESS:
                mCurrentPassField.setVisibility(View.VISIBLE);
                mCurrentPassField.setEnabled(true);
                mCurrentPassField.setText("");
                mNewPassField.setVisibility(View.VISIBLE);
                mNewPassField.setEnabled(true);
                mNewPassField.setText("");
                mConfirmNewPassField.setVisibility(View.VISIBLE);
                mConfirmNewPassField.setEnabled(true);
                mConfirmNewPassField.setText("");
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
    public Object ServerEventHandler = new Object() {

        @Subscribe
        public void onSuccessEvent(AccountUpdateEvent response) {

            if(!isDetached() || !isRemoving()) {

                updateState(State.SUCCESS);

                if(isListening) {
                    isListening = false;
                    mButton.setType(TransitionButton.BtnType.SUCCESS)
                            .setFromText(getResources().getString(R.string.update))
                            .setToText(getResources().getString(R.string.account_details_updated))
                            .setDuration(4000)
                            .start();
                }

                refreshViews();

                AppUtils.setPass(mNewPassField.getText().toString());

                AppUtils.setAuth();
            }

        }

        //Subscribed to receive ErrorEvents
        @Subscribe
        public void onErrorEvent(ErrorEvent errorEvent) {

            if(!isDetached() || !isRemoving()) {

                updateState(State.ERROR);

                //Checks to see if there is an internet connection
                //This is to let the Volley API determine if it cannot establish a connection
                if (!errorEvent.getErrorDescription().equals(AppUtils.NO_CONNECTION)) {

                    String errorDescription = getErrorDescription(errorEvent);

                    mButton.setType(TransitionButton.BtnType.ERROR)
                            .setFromText(getResources().getString(R.string.update))
                            .setToText(errorDescription)
                            .setDuration(4000)
                            .start();

                    YoYo.with(Techniques.Shake).duration(1500).playOn(mNewPassField);
                    YoYo.with(Techniques.Shake).duration(1500).playOn(mConfirmNewPassField);

                } else {
                    mButton.setType(TransitionButton.BtnType.ERROR)
                            .setFromText(mButton.getText().toString())
                            .setToText(getResources().getString(R.string.check_internet))
                            .setDuration(4000)
                            .start();
                }
            }
        }
    };
}

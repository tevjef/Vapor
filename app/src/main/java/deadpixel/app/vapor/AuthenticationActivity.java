package deadpixel.app.vapor;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.VolleyError;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.apache.http.HttpStatus;


import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.callbacks.ResponseEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.impl.CloudAppImpl;
import deadpixel.app.vapor.libs.EaseOutQuint;
import deadpixel.app.vapor.libs.TransitionButton;
import deadpixel.app.vapor.networkOp.AuthenticationTaskFragment;
import deadpixel.app.vapor.utils.AppUtils;
import deadpixel.app.vapor.utils.AppUtils.TextStyle;

public class AuthenticationActivity extends SherlockFragmentActivity implements AuthenticationTaskFragment.TaskCallbacks {

    private static final boolean DEBUG = true;
    String TAG = "AuthenticationActivity";
    public static SharedPreferences prefs;
    private Context context;
    private static EditText mEmailField;
    private static String userEmail;
    private static EditText mPassField;
    private static EditText mConfirmPassField;
    private String mConfirmPass;

    private State mState;
    private SubState mSubState;

    private String userPass;
    private TransitionButton mButton;
    private TextView tos;
    private Activity mActivty;

    private static final String TAG_TASK_FRAGMENT = "authentication_task_fragment";
    private AuthenticationTaskFragment mAuthenticationTaskFragment;

    boolean signedIn;

    private TextSwitcher mTextSwitch;

    private SharedPreferences.Editor editPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        //sets the XML layout

        //Instantiates a preference to be acted upon.
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editPref = prefs.edit();

        boolean startupAnimationRan = prefs.getBoolean(AppUtils.STARTUP_ANIMATION_RAN, false);

        boolean signedIn = AppUtils.isSignedIn();


        if(!signedIn) {
            setContentView(R.layout.activity_login);
            //sets the Actionbar and its properties
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);

            //stores the application context for future use.
            context = getApplicationContext();

            setUpActivityFields();

            if(!startupAnimationRan) {
                startUpAnimation();
            } else {
                updateStateFromPreferences();
                final ViewGroup fieldsContainerLayout = (ViewGroup) findViewById(R.id.fields_contain_layout);
                fieldsContainerLayout.setVisibility(View.VISIBLE);
                mEmailField.setVisibility(View.VISIBLE);
                mPassField.setVisibility(View.VISIBLE);
                mButton.setVisibility(View.VISIBLE);
                tos.setVisibility(View.VISIBLE);

                ViewGroup containerLayout = (ViewGroup) findViewById(R.id.field_group);
                LayoutTransition layoutTransition = new LayoutTransition();
                containerLayout.setLayoutTransition(layoutTransition);
            }

            FragmentManager fm = getSupportFragmentManager();
            mAuthenticationTaskFragment = (AuthenticationTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

            // If the Fragment is non-null, then it is currently being
            // retained across a configuration change.
            if (mAuthenticationTaskFragment == null) {
                mAuthenticationTaskFragment = new AuthenticationTaskFragment();
                fm.beginTransaction().add(mAuthenticationTaskFragment, TAG_TASK_FRAGMENT).commit();
            }

        } else {
            Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    public void updateStateFromPreferences() {
        final int state = prefs.getInt(AppUtils.AUTH_ACTIVITY_STATE, 2);
        if(state == -1) {
            updateState(State.SIGNIN, SubState.NORMAL);
        } else if(state == 0) {
            updateState(State.REGISTER, SubState.NORMAL);

        } else if(state == 1) {
            updateState(State.FORGOTPASS, SubState.NORMAL);
        } else {
            updateState(State.SIGNIN, SubState.NORMAL);
            if(AppUtils.GLOBAL_DEBUG || DEBUG) {
                Log.e(TAG, "Couldn't updateState from preferences");
            }
        }
    }

    protected void onPostCreate(Bundle savedStateInstance) {
        super.onPostCreate(savedStateInstance);
    }


    private void setUpActivityFields() {

        //Saves a reference to the current activity to use in anonymous classes.
        mActivty = this;



        //Holds reference to the various views on screen.
        tos = (TextView) findViewById(R.id.tos);
        mEmailField = (EditText) findViewById(R.id.email_field);
        mPassField = (EditText) findViewById(R.id.pass_field);
        mConfirmPassField = (EditText) findViewById(R.id.confirm_pass_field);
        mButton = (TransitionButton) findViewById(R.id.btn_toggle);

        //Sets the typeface for the email field
        mEmailField.setTypeface(AppUtils.getTextStyle(TextStyle.LIGHT_NORMAL));

        //Sets the typeface for the password field
        mPassField.setTypeface(AppUtils.getTextStyle(TextStyle.LIGHT_NORMAL));

        //Sets the typeface for the password field
        mConfirmPassField.setTypeface(AppUtils.getTextStyle(TextStyle.LIGHT_NORMAL));


        //Sets up the Transition butt to receive animation events.
        mButton.with(this)
                .setNormalSelector(R.drawable.btn_toggle_selector)
                .setErrorSelector(R.drawable.btn_toggle_error_selector)
                .setSuccessSelector(R.drawable.btn_toggle_success_selector);

        //performs action when user clicks the 'next' button on their soft keyboard.
        mPassField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mButton.performClick();
                    handled = true;
                }
                return handled;
            }
        });
        //For debugging purposes
        mEmailField.setText("mastermindtj94@gmail.com");
        mPassField.setText("master123");

        //Special lister to change state to Sign In after user successfully sends and email to a recovery address
        mButton.setTransitionListener(new TransitionButton.OnTransitionListener() {
            @Override
            public void OnTransitionStart(TransitionButton button) {
                mButton.setEnabled(false);
            }
            @Override
            public void OnTransitionEnd(TransitionButton button) {
                mButton.setEnabled(true);
                supportInvalidateOptionsMenu();
                if (mState == State.FORGOTPASS && mSubState == SubState.SUCCESS)
                    updateState(State.SIGNIN, SubState.NORMAL);
            }
        });

    }

    public void startUpAnimation() {

        editPref.putBoolean(AppUtils.STARTUP_ANIMATION_RAN, true).commit();

        ViewGroup containerLayout = (ViewGroup) findViewById(R.id.contain_layout);
        final LayoutTransition layoutTransition  = new LayoutTransition();
        containerLayout.setLayoutTransition(layoutTransition);

        final ViewGroup fieldsContainerLayout = (ViewGroup) findViewById(R.id.fields_contain_layout);

        //Fades in the logo
        YoYo.with(Techniques.FadeIn).duration(2500).playOn(findViewById(R.id.login_logo));

        //A concerted effort to animated fields on startup
        YoYo.with(Techniques.FadeInUp).duration(2500).withListener(new com.nineoldandroids.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {

            }

            @Override
            public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {

                fieldsContainerLayout.setVisibility(View.VISIBLE);

                YoYo.with(Techniques.FadeInUp).duration(500).withListener(new com.nineoldandroids.animation.Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                        mEmailField.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {

                        YoYo.with(Techniques.FadeInUp).duration(500).withListener(new com.nineoldandroids.animation.Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                                mPassField.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {

                                YoYo.with(Techniques.FadeInUp).duration(500).withListener(new com.nineoldandroids.animation.Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                                        mButton.setVisibility(View.VISIBLE);
                                        mButton.setEnabled(false);
                                    }

                                    @Override
                                    public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {


                                        YoYo.with(Techniques.FadeInUp).duration(200).withListener(new com.nineoldandroids.animation.Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                                                tos.setVisibility(View.VISIBLE);

                                            }

                                            @Override
                                            public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {

                                                ViewGroup containerLayout = (ViewGroup) findViewById(R.id.field_group);
                                                LayoutTransition layoutTransition = new LayoutTransition();
                                                containerLayout.setLayoutTransition(layoutTransition);
                                            }

                                            @Override
                                            public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {

                                            }

                                            @Override
                                            public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {

                                            }
                                        }).playOn(tos);

                                        updateState(State.SIGNIN, SubState.NORMAL);
                                        mButton.setEnabled(true);
                                    }

                                    @Override
                                    public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {

                                    }
                                }).interpolate(new EaseOutQuint()).playOn(mButton);
                            }

                            @Override
                            public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {

                            }
                        }).interpolate(new EaseOutQuint()).playOn(mPassField);

                    }

                    @Override
                    public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {

                    }
                }).interpolate(new EaseOutQuint()).playOn(mEmailField);


            }

            @Override
            public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {

            }

            @Override
            public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {

            }
        }).playOn(findViewById(R.id.login_logo_text));


    }

    //Provides an easy way to to shake Views
    private void shake(View v){
        YoYo.with(Techniques.Shake).duration(1500).playOn(v);
    }

    private boolean isSignInFieldValid() {

        userEmail = mEmailField.getText().toString();
        userPass = mPassField.getText().toString();

        //If the email field is empty.
        if (AppUtils.isEmpty(userEmail)) {
            shake(mEmailField);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_email))
                    .start();

            updateState(getState(), SubState.ERROR);
            return false;

            //If the email field has errors
        } else if (!AppUtils.isValidEmail(userEmail)) {
            shake(mEmailField);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.invalid_email))
                    .start();

            updateState(getState(), SubState.ERROR);
            return false;
            //If password field is empty
        } else if (AppUtils.isEmpty(userPass)) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mPassField);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_password))
                    .start();

            updateState(getState(), SubState.ERROR);
            return false;
        } else {
            return true;
        }

    }
    private boolean isRegisterFieldsValid() {

        userEmail = mEmailField.getText().toString();
        userPass = mPassField.getText().toString();
        mConfirmPass = mConfirmPassField.getText().toString();

        //If the email field is empty.
        if (AppUtils.isEmpty(userEmail)) {
            shake(mEmailField);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_email))
                    .start();

            updateState(getState(), SubState.ERROR);
            return false;

            //If the email field has errors
        } else if (!AppUtils.isValidEmail(userEmail)) {
            shake(mEmailField);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.invalid_email))
                    .start();

            updateState(getState(), SubState.ERROR);
            return false;
            //If password field is empty
        } else if (AppUtils.isEmpty(userPass)) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mPassField);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_password))
                    .start();

            updateState(getState(), SubState.ERROR);
            return false;
        } else if (AppUtils.isEmpty(mConfirmPass)) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mConfirmPassField);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_password))
                    .start();

            updateState(getState(), SubState.ERROR);
            return false;
        } else if (!mPassField.getText().toString().equals(mConfirmPassField.getText().toString())) {
            YoYo.with(Techniques.Shake).duration(1500).playOn(mConfirmPassField);
            YoYo.with(Techniques.Shake).duration(1500).playOn(mPassField);

            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.invalid_pass_no_match))
                    .start();

            updateState(getState(), SubState.ERROR);
            return false;
        }else {
            return true;
        }
    }
    private boolean isForgotPassFieldsValid() {

        userEmail = mEmailField.getText().toString();
        userPass = mPassField.getText().toString();

        //If email field is empty
        if (AppUtils.isEmpty(userEmail)) {
            shake(mEmailField);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.empty_email))
                    .start();

            updateState(getState(), SubState.ERROR);
            return false;
            //If the email field has errors
        } else if (!AppUtils.isValidEmail(userEmail)) {
            shake(mEmailField);
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setTransitionText(mButton.getText(), getResources().getString(R.string.invalid_email))
                    .start();

            updateState(getState(), SubState.ERROR);
            return false;
        }
        return true;
    }
    private boolean isValidFields() {

        switch(mState) {
            case SIGNIN:
                return isSignInFieldValid();
            case REGISTER:
                return isRegisterFieldsValid();
            case FORGOTPASS:
                return isForgotPassFieldsValid();
            default:
                return false;
        }
    }

    private void signIn() {

            AppUtils.addToRequestQueue(AppUtils.api.requestAccountDetails());
    }

    private void createAccount() {

        try {

         AppUtils.addToRequestQueue(AppUtils.api.createAccount(AppUtils.getEmail(), AppUtils.getPass(), true));

        } catch (CloudAppException e) {
            e.printStackTrace();
        }

    }

    private void resetPassword() {
        try {
            AppUtils.addToRequestQueue(new CloudAppImpl(this).resetPassword(getEmail()));
        } catch (CloudAppException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void btnToggle(View view) {


        if(isValidFields()){

            //Sets the substate to 'progress' to that the text changes and the fields get disabled
            updateState(getState(), SubState.PROGRESS);

            //Saves the credentials to the obscured credentials to preferences
            setCredentials(userEmail, userPass);



            switch (mState) {
                case SIGNIN:

                    signIn();

                    break;
                case REGISTER:

                    createAccount();

                    break;
                case FORGOTPASS:

                    resetPassword();

                    break;
            }
        }

    }

    public void tosDialog(View v) {
        String url = "https://github.com/cloudapp/policy/blob/master/terms-of-service.md";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    //Saves the credentials to the obscured credentials to preferences
    public void setCredentials(String email, String pass) {

        AppUtils.setEmail(email);
        AppUtils.setPass(pass);

    }

    public static String getEmail() {
        return prefs.getString("email", null);
    }
    public static String getPass() {
        return prefs.getString("pass", null);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mSubState == SubState.PROGRESS) {
            return false;
        }
        else if(mButton.isTransitioning())
            return false;
        else {
            if(mState == State.SIGNIN) {
                menu.clear();
                editPref.putInt(AppUtils.AUTH_ACTIVITY_STATE, -1);
                menu.add(Menu.NONE, 2, Menu.NONE, getResources().getString(R.string.lower_register));
                menu.add(Menu.NONE, 3, Menu.NONE, getResources().getString(R.string.forgot_password));

            } else if(mState == State.REGISTER){
                menu.clear();
                editPref.putInt(AppUtils.AUTH_ACTIVITY_STATE, 0);
                menu.add(Menu.NONE, 1, Menu.NONE, getResources().getString(R.string.lower_sign_in));
                menu.add(Menu.NONE, 3, Menu.NONE, getResources().getString(R.string.forgot_password));

            } else if(mState == State.FORGOTPASS) {
                menu.clear();
                editPref.putInt(AppUtils.AUTH_ACTIVITY_STATE, 1);
                menu.add(Menu.NONE, 1, Menu.NONE, getResources().getString(R.string.lower_sign_in));
                menu.add(Menu.NONE, 2, Menu.NONE, getResources().getString(R.string.lower_register));
            }

            return super.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mAuthenticationTaskFragment = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 1:

                updateState(State.SIGNIN, SubState.NORMAL);

                return true;

            case 2:

                updateState(State.REGISTER, SubState.NORMAL);

                return true;

            case 3:
                updateState(State.FORGOTPASS, SubState.NORMAL);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void setSignedIn() {
        editPref.putBoolean(AppUtils.SIGNED_IN, true);
        editPref.apply();
    }


    private void setNormalButtonStateText() {
        switch(mState) {
            case SIGNIN:
                mButton.setFromText(getResources().getString(R.string.sign_in));
                break;
            case REGISTER:
                mButton.setFromText(getResources().getString(R.string.create_account));
                break;
            case FORGOTPASS:
                mButton.setFromText(getResources().getString(R.string.forgot_password));
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
                switch (volleyError.networkResponse.statusCode) {
                    case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                        errorDescription = getResources().getString(R.string.invalid_email_password);
                        break;
                    case HttpStatus.SC_UNAUTHORIZED:
                        errorDescription = getResources().getString(R.string.incorrect_email_pass);
                        break;
                    case HttpStatus.SC_NOT_ACCEPTABLE:
                        errorDescription = getResources().getString(R.string.account_already_exists);
                        break;
                    default:
                        break;
                }
            }
        }
        return errorDescription;
    }

    @Override
    public void onAccountUpdate(final CloudAppAccount account) {
        updateState(getState(), SubState.SUCCESS);

        setNormalButtonStateText();

        if (mSubState== SubState.SUCCESS && mState != State.FORGOTPASS) {
            mButton.setType(TransitionButton.BtnType.SUCCESS)
                    .setToText(getResources().getString(R.string.success))
                    .setDuration(1500)
                    .setIndefinite(true)
                    .start();

            setSignedIn();

            mButton.setTransitionListener(new TransitionButton.OnTransitionListener() {
                @Override
                public void OnTransitionStart(TransitionButton button) {

                }

                @Override
                public void OnTransitionEnd(TransitionButton button) {
                    supportInvalidateOptionsMenu();
                    switch (mState) {
                        case SIGNIN:

                            try {
                                if(account.getEmail().equals(getEmail())) {
                                    Intent intent = new Intent(mActivty, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                            } catch (CloudAppException e) {
                                Log.e(TAG, e.getMessage());
                            }
                            break;
                        case REGISTER:
                            try {

                                if(account.getEmail().equals(getEmail())) {
                                    Intent intent = new Intent(mActivty, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            } catch (CloudAppException e) {
                                Log.e(TAG, e.getMessage());
                            }
                            break;

                        default:
                            break;
                    }
                }
            });
        } else {
            mButton.setType(TransitionButton.BtnType.SUCCESS)
                    .setFromText(getResources().getString(R.string.sign_in))
                    .setToText(getResources().getString(R.string.instructions))
                    .setDuration(4000)
                    .start();
        }
    }

    @Override
    public void onServerResponse(ResponseEvent event) {
        updateState(getState(), SubState.SUCCESS);

        setNormalButtonStateText();

        if(getState() == State.FORGOTPASS) {
            mButton.setType(TransitionButton.BtnType.SUCCESS)
                    .setFromText(getResources().getString(R.string.sign_in))
                    .setToText(getResources().getString(R.string.instructions))
                    .setDuration(4000)
                    .start();
        }
    }

    @Override
    public void onErrorEvent(ErrorEvent errorEvent) {
        updateState(getState(), SubState.ERROR);

        //Checks to see if there is an internet connection
        //This is to let the Volley API determine if it cannot establish a connection
        setNormalButtonStateText();

        if(!errorEvent.getExplicitError().equals(AppUtils.NO_CONNECTION)) {

            String errorDescription = getErrorDescription(errorEvent);

            mButton.setToText(errorDescription)
                    .setType(TransitionButton.BtnType.ERROR)
                    .start();
            //AppUtils.makeCrouton(mActivty, errorDescription, Style.ALERT);
            YoYo.with(Techniques.Shake).duration(1500).playOn(mEmailField);
            YoYo.with(Techniques.Shake).duration(1500).playOn(mPassField);
            YoYo.with(Techniques.Shake).duration(1500).playOn(mConfirmPassField);

        } else {
            mButton.setType(TransitionButton.BtnType.ERROR)
                    .setToText(getResources().getString(R.string.check_internet))
                    .start();
        }
    }

    private enum State {
        SIGNIN, REGISTER, FORGOTPASS;

    }

    private enum SubState {
        NORMAL, PROGRESS, ERROR, SUCCESS;
    }

    private State getState() {
        return mState;
    }

    private void setState(State mState) {
        this.mState = mState;
    }

    private SubState getSubState() {
        return mSubState;
    }

    private void setSubState(SubState mSubState) {
        this.mSubState = mSubState;
    }

    private void updateState(State state, SubState subState) {
        setState(state);
        setSubState(subState);


        switch (mState) {
            case SIGNIN:
                switch (mSubState) {
                    case NORMAL:
                        mEmailField.setEnabled(true);
                        mPassField.setEnabled(true);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.VISIBLE);
                        mConfirmPassField.setVisibility(View.GONE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mPassField.setImeActionLabel(getResources().getString(R.string.lower_sign_in), mPassField.getImeActionId());
                        mButton.setText(getResources().getText(R.string.sign_in));

                        supportInvalidateOptionsMenu();
                        break;
                    case PROGRESS:
                        mEmailField.setEnabled(false);
                        mPassField.setEnabled(false);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.VISIBLE);
                        mConfirmPassField.setVisibility(View.GONE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(false);
                        mButton.setText(getResources().getText(R.string.signing_in));
                        mPassField.setImeActionLabel(getResources().getString(R.string.lower_sign_in), mPassField.getImeActionId());
                        supportInvalidateOptionsMenu();
                        break;
                    case ERROR:

                        mEmailField.setEnabled(true);
                        mPassField.setEnabled(true);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.VISIBLE);
                        mConfirmPassField.setVisibility(View.GONE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mPassField.setImeActionLabel(getResources().getString(R.string.lower_sign_in), mPassField.getImeActionId());
                        supportInvalidateOptionsMenu();
                        break;
                    case SUCCESS:
                        mEmailField.setEnabled(false);
                        mPassField.setEnabled(false);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.VISIBLE);
                        mConfirmPassField.setVisibility(View.GONE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mPassField.setImeActionLabel(getResources().getString(R.string.lower_sign_in), mPassField.getImeActionId());
                        supportInvalidateOptionsMenu();
                        break;
                    default:
                        break;

                }
                break;
            case REGISTER:
                switch (mSubState) {
                    case NORMAL:
                        mEmailField.setEnabled(true);
                        mPassField.setEnabled(true);
                        mConfirmPassField.setEnabled(true);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.VISIBLE);
                        mConfirmPassField.setVisibility(View.VISIBLE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mPassField.setImeActionLabel(getResources().getString(R.string.lower_register),  mPassField.getImeActionId());
                        mButton.setText(getResources().getText(R.string.create_account));
                        supportInvalidateOptionsMenu();
                        break;
                    case PROGRESS:
                        mEmailField.setEnabled(false);
                        mPassField.setEnabled(false);
                        mConfirmPassField.setEnabled(false);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.VISIBLE);
                        mConfirmPassField.setVisibility(View.VISIBLE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(false);
                        mPassField.setImeActionLabel(getResources().getString(R.string.lower_register),  mPassField.getImeActionId());
                        mButton.setText(getResources().getText(R.string.creating_account));
                        supportInvalidateOptionsMenu();
                        break;
                    case ERROR:
                        mEmailField.setEnabled(true);
                        mPassField.setEnabled(true);
                        mConfirmPassField.setEnabled(true);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.VISIBLE);
                        mConfirmPassField.setVisibility(View.VISIBLE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mPassField.setImeActionLabel(getResources().getString(R.string.lower_register),  mPassField.getImeActionId());


                        supportInvalidateOptionsMenu();
                        break;
                    case SUCCESS:
                        mEmailField.setEnabled(false);
                        mPassField.setEnabled(false);
                        mConfirmPassField.setEnabled(false);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.VISIBLE);
                        mConfirmPassField.setVisibility(View.VISIBLE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mPassField.setImeActionLabel(getResources().getString(R.string.lower_register),  mPassField.getImeActionId());
                        supportInvalidateOptionsMenu();
                        break;
                    default:
                        break;
                }
                break;
            case FORGOTPASS:
                switch (mSubState) {
                    case NORMAL:
                        mEmailField.setEnabled(true);
                        mPassField.setEnabled(false);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.GONE);
                        mConfirmPassField.setVisibility(View.GONE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mEmailField.setImeActionLabel(getResources().getString(R.string.send),  mEmailField.getImeActionId());
                        mButton.setText(getResources().getText(R.string.recover_pass));
                        supportInvalidateOptionsMenu();
                        break;
                    case PROGRESS:
                        mEmailField.setEnabled(false);
                        mPassField.setEnabled(false);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.GONE);
                        mConfirmPassField.setVisibility(View.GONE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(false);
                        mPassField.setImeActionLabel(getResources().getString(R.string.send),  mEmailField.getImeActionId());
                        mButton.setText(getResources().getText(R.string.recovering_pass));
                        supportInvalidateOptionsMenu();
                        break;
                    case ERROR:
                        mEmailField.setEnabled(true);
                        mPassField.setEnabled(false);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.GONE);
                        mConfirmPassField.setVisibility(View.GONE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mEmailField.setImeActionLabel(getResources().getString(R.string.send),  mEmailField.getImeActionId());
                        
                        supportInvalidateOptionsMenu();
                        break;
                    case SUCCESS:
                        mEmailField.setEnabled(true);
                        mPassField.setEnabled(false);
                        mEmailField.setVisibility(View.VISIBLE);
                        mPassField.setVisibility(View.GONE);
                        mConfirmPassField.setVisibility(View.GONE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mEmailField.setImeActionLabel(getResources().getString(R.string.send),  mEmailField.getImeActionId());
                        supportInvalidateOptionsMenu();

                        break;
                    default:
                        break;
                }
                break;
            default:
                break;

        }

    }

    private void animateError() {

    }
}

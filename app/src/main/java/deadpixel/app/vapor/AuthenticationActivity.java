package deadpixel.app.vapor;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import deadpixel.app.vapor.callbacks.AccountUpdateCallback;
import deadpixel.app.vapor.callbacks.ObserverCollection;
import deadpixel.app.vapor.callbacks.ResponseCallback;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.impl.AccountImpl;
import deadpixel.app.vapor.networkOp.authentication.ObscuredSharedPreferences;
import deadpixel.app.vapor.cloudapp.api.CloudApp;
import deadpixel.app.vapor.cloudapp.impl.CloudAppImpl;
import deadpixel.app.vapor.networkOp.RequestHandler;
import deadpixel.app.vapor.utils.AppUtils;


public class AuthenticationActivity extends SherlockActivity implements ResponseCallback {

    String PREF_NAME = "deadpixel.app.vapor";
    public static SharedPreferences prefs;
    protected Context context;
    protected static EditText emailField;
    protected String userEmail;
    protected static EditText passField;
    protected String userPass;
    protected Button btn_toggle;
    protected static ProgressDialog progress;
    protected static boolean activityLoginState;

    CloudApp api;
    CloudAppAccount account;
    String response;
    Drawable noStroke;
    Drawable stroke;
    Animation ani1;
    Animation ani2;

    protected ObscuredSharedPreferences.Editor editPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sets the XML layout
        setContentView(R.layout.activity_login);
        //sets the Actionbar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);


        //stores the application context for future use.
        context = getApplicationContext();

        setUpActivityFields();
        api = new CloudAppImpl(this);

    }


    private void setUpActivityFields() {

        prefs = new ObscuredSharedPreferences(
                this, this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) );

        stroke = getResources().getDrawable(R.drawable.edittext);
        noStroke  = getResources().getDrawable(R.drawable.edittext_error);
        emailField = (EditText) findViewById(R.id.email_field);
        passField = (EditText) findViewById(R.id.pass_field);
        passField.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
       // checkbox = (CheckBox) findViewById(R.id.showpassword);
        btn_toggle = (Button) findViewById(R.id.btn_toggle);

        emailField.setText("mastermindtj94@gmail.com");
        passField.setText("master123");
        emailField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailField.setBackgroundResource(R.drawable.edittext);
            }
        });

        passField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    btn_toggle.performClick();
                    handled = true;
                }
                return handled;
            }
        });
        passField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passField.setBackgroundResource(R.drawable.edittext);
            }
        });

        /*checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    passField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passField.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                } else {
                    passField.setInputType(129);
                    passField.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                }
            }
        });*/

        updateState(State.SIGNIN, SubState.NORAML);
    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void btnToggle(View view) {

        updateState(getState(), SubState.PROGRESS);


        userEmail = emailField.getText().toString();
        userPass = passField.getText().toString();

        if(!AppUtils.isValidEmail(userEmail)) {
            Toast.makeText(context, "Invalid email address", Toast.LENGTH_SHORT).show();
            updateState(getState(), SubState.ERROR);
        }
        else if(!AppUtils.isValidPass(userPass)) {
            Toast.makeText(context, "Password field is empty", Toast.LENGTH_SHORT).show();
            updateState(getState(), SubState.ERROR);
        }
        else {
            setCredentials(userEmail, userPass);

            //registers an observer for a network event
            ObserverCollection.getInstance().registerObserver("AuthenticationActivity", this);

            switch (mState) {
                case SIGNIN:

                try {
                    api.requestAccountDetails();

                } catch (CloudAppException e) {
                    e.getMessage();
                }
                    break;
                case REGISTER:
                    try {
                        api.createAccount(getEmail(), getPass(), true);
                    } catch (CloudAppException e) {
                        e.printStackTrace();
                    }
                    break;
                case FORGOTPASS:

                    try {
                        api.resetPassword(getEmail());
                    } catch (CloudAppException e) {
                        e.printStackTrace();
                    }
            }
        }

    }

    public void tosDialog(View v) {
        String url = "https://github.com/cloudapp/policy/blob/master/terms-of-service.md";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void toggleBoxError() {
        if (emailField.getBackground() == noStroke) {
            emailField.setBackground(stroke);
            passField.setBackground(stroke);
        } else {
            emailField.setBackground(noStroke);
            passField.setBackground(noStroke);
        }
    }

    public static void startProgress(){
        if (activityLoginState) {
            progress.setMessage("Signing in...");
        } else {
            progress.setMessage("Creating account");
        }

        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
    }


    public void setCredentials(String email, String pass) {
        editPref = (ObscuredSharedPreferences.Editor) prefs.edit();
        editPref.putString("ZW1haWw=", email);
        editPref.putString("cGFzcw==", pass);
        Log.i("Credentials saved to Preferences", email + "  " + pass);
        editPref.apply();

        //sets authentication of the HTTP client
        RequestHandler.setHttpAuthentication(getEmail(), getPass());

    }

    public static String getEmail() {
        return prefs.getString("ZW1haWw=", null);
    }
    public static String getPass() {
        return prefs.getString("cGFzcw==", null);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mSubState == SubState.PROGRESS) {
            return false;
        } else {
            if(mState == State.SIGNIN) {
                menu.clear();
                menu.add(Menu.NONE, 2, Menu.NONE, getResources().getString(R.string.register));
                menu.add(Menu.NONE, 3, Menu.NONE, getResources().getString(R.string.forgot_password));

            } else if(mState == State.REGISTER){
                menu.clear();
                menu.add(Menu.NONE, 1, Menu.NONE, getResources().getString(R.string.login));
                menu.add(Menu.NONE, 3, Menu.NONE, getResources().getString(R.string.forgot_password));

            } else if(mState == State.FORGOTPASS) {
                menu.clear();
                menu.add(Menu.NONE, 1, Menu.NONE, getResources().getString(R.string.login));
                menu.add(Menu.NONE, 2, Menu.NONE, getResources().getString(R.string.register));
            }

            return super.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 1:
                updateState(State.SIGNIN, SubState.NORAML);

                return true;

            case 2:
                updateState(State.REGISTER, SubState.NORAML);
                return true;

            case 3:
                updateState(State.FORGOTPASS, SubState.NORAML);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public void onServerResponse(String response) {
        updateState(getState(), SubState.SUCCESS);

        api.updateAccountDetails(response);

        switch (mState) {
            case SIGNIN:
                try {
                    account = api.getAccountDetails();

                  if(account.getEmail().equals(getEmail())) {
                      Intent intent = new Intent(this, MainActivity.class);
                      startActivity(intent);
                  }

                } catch (CloudAppException e) {
                    e.printStackTrace();
                }
                break;
            case REGISTER:
                try {
                    account = api.getAccountDetails();

                    if(account.getEmail().equals(getEmail())) {
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    }
                } catch (CloudAppException e) {
                    e.printStackTrace();
                }
                break;
            case FORGOTPASS:

                updateState(State.SIGNIN, SubState.NORAML);

                break;

            default:
                mState = State.SIGNIN;
        }

        ObserverCollection.getInstance().unRegisterObserver("AuthenticationActivity", this);
    }

    @Override
    public void onServerError(VolleyError e, String errorDescription) {
        updateState(getState(), SubState.ERROR);
        toggleBoxError();

        AppUtils appUtils = new AppUtils(getApplicationContext());
        if(appUtils.isNetworkConnected()) {
            Toast toast = Toast.makeText(this, errorDescription, Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "No internet connection" , Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private enum State {
        SIGNIN, REGISTER, FORGOTPASS;

    }

    private enum SubState {
        NORAML, PROGRESS, ERROR, SUCCESS;
    }

    public State getState() {
        return mState;
    }

    public void setState(State mState) {
        this.mState = mState;
    }

    public SubState getSubState() {
        return mSubState;
    }

    public void setSubState(SubState mSubState) {
        this.mSubState = mSubState;
    }

    private State mState;
    private SubState mSubState;

    private void updateState(State state, SubState subState) {
        setState(state);
        setSubState(subState);

        switch (mState) {
            case SIGNIN:
                switch (subState) {
                    case NORAML:
                        btn_toggle.setBackgroundResource(R.drawable.btn_toggle_selector);
                        emailField.setEnabled(true);
                        passField.setEnabled(true);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.VISIBLE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(true);
                        btn_toggle.setText(getResources().getText(R.string.sign_in));
                        supportInvalidateOptionsMenu();
                        break;
                    case PROGRESS:
                        emailField.setEnabled(false);
                        passField.setEnabled(false);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.VISIBLE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(false);
                        btn_toggle.setText(getResources().getText(R.string.signing_in));
                        supportInvalidateOptionsMenu();
                        break;
                    case ERROR:
                        emailField.setEnabled(true);
                        passField.setEnabled(true);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.VISIBLE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(true);
                        btn_toggle.setText(getResources().getText(R.string.sign_in));
                        supportInvalidateOptionsMenu();
                        break;
                    case SUCCESS:
                        emailField.setEnabled(true);
                        passField.setEnabled(true);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.VISIBLE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(true);
                        btn_toggle.setText(getResources().getText(R.string.success));;
                        btn_toggle.setBackgroundResource(R.drawable.btn_toggle_success);
                        supportInvalidateOptionsMenu();
                        break;
                    default:
                        break;

                }
                break;
            case REGISTER:
                switch (subState) {
                    case NORAML:
                        btn_toggle.setBackgroundResource(R.drawable.btn_toggle_selector);
                        emailField.setEnabled(true);
                        passField.setEnabled(true);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.VISIBLE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(true);
                        btn_toggle.setText(getResources().getText(R.string.create_account));
                        supportInvalidateOptionsMenu();
                        break;
                    case PROGRESS:
                        emailField.setEnabled(false);
                        passField.setEnabled(false);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.VISIBLE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(false);
                        btn_toggle.setText(getResources().getText(R.string.creating_account));
                        supportInvalidateOptionsMenu();
                        break;
                    case ERROR:
                        emailField.setEnabled(true);
                        passField.setEnabled(true);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.VISIBLE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(true);
                        btn_toggle.setText(getResources().getText(R.string.create_account));
                        supportInvalidateOptionsMenu();
                        break;
                    case SUCCESS:
                        emailField.setEnabled(true);
                        passField.setEnabled(true);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.VISIBLE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(true);
                        btn_toggle.setText(getResources().getText(R.string.success));
                        btn_toggle.setBackgroundResource(R.drawable.btn_toggle_success);
                        supportInvalidateOptionsMenu();
                        break;
                    default:
                        break;
                }
                break;
            case FORGOTPASS:
                switch (subState) {
                    case NORAML:
                        btn_toggle.setBackgroundResource(R.drawable.btn_toggle_selector);
                        emailField.setEnabled(true);
                        passField.setEnabled(false);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.GONE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(true);
                        btn_toggle.setText(getResources().getText(R.string.recover_pass));
                        supportInvalidateOptionsMenu();
                        break;
                    case PROGRESS:
                        emailField.setEnabled(false);
                        passField.setEnabled(false);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.GONE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(false);
                        btn_toggle.setText(getResources().getText(R.string.recovering_pass));
                        supportInvalidateOptionsMenu();
                        break;
                    case ERROR:
                        emailField.setEnabled(true);
                        passField.setEnabled(false);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.GONE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(true);
                        btn_toggle.setText(getResources().getText(R.string.recover_pass));
                        supportInvalidateOptionsMenu();
                        break;
                    case SUCCESS:
                        emailField.setEnabled(true);
                        passField.setEnabled(false);
                        emailField.setVisibility(View.VISIBLE);
                        passField.setVisibility(View.GONE);
                        btn_toggle.setVisibility(View.VISIBLE);
                        btn_toggle.setEnabled(true);
                        btn_toggle.setText(getResources().getText(R.string.success));
                        btn_toggle.setBackgroundResource(R.drawable.btn_toggle_success);
                        Toast.makeText(context, "Check your email for recovery instructions", Toast.LENGTH_SHORT).show();
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
}

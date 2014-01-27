package deadpixel.app.vapor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import deadpixel.app.vapor.authentication.Authenticate;
import deadpixel.app.vapor.authentication.ObscuredSharedPreferences;
import deadpixel.app.vapor.cloudapp.api.CloudApp;
import deadpixel.app.vapor.cloudapp.impl.CloudAppImpl;
import deadpixel.app.vapor.networkOp.RequestHandler;


public class AuthenticationActivity extends ActionBarActivity {

    String PREF_NAME = "deadpixel.app.vapor";
    public static SharedPreferences prefs;
    protected Context context;
    protected EditText emailField;
    protected String userEmail;
    protected EditText passField;
    protected String userPass;
    protected Button btn_toggle;
    protected CloudApp api = new CloudAppImpl();
    protected static ProgressDialog progress;
    protected boolean activityLoginState;
    protected CheckBox checkbox;

    protected ObscuredSharedPreferences.Editor editPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        getApplicationContext();
        context = getApplicationContext();
        prefs = new ObscuredSharedPreferences(
                this, this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) );

        emailField = (EditText) findViewById(R.id.email_field);
        passField = (EditText) findViewById(R.id.pass_field);
        passField.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        checkbox = (CheckBox) findViewById(R.id.showpassword);

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        });

        activityLoginState = true;

        btn_toggle = (Button) findViewById(R.id.btn_toggle);
        progress = new ProgressDialog(this);
    }



    public void btnToggle(View view) {

        String userEmail = emailField.getText().toString();
        String userPass = passField.getText().toString();

        if(userEmail.matches(""))
            Toast.makeText(context, "Email field is empty", Toast.LENGTH_SHORT).show();
        else if(userPass.matches(""))
            Toast.makeText(context, "Password field is empty", Toast.LENGTH_SHORT).show();
        else {
            setCredentials(userEmail, userPass);
            Log.i("Credentials saved to Preferences", userEmail + userPass);

            if (activityLoginState) {
                RequestHandler.setHttpAuthentication(getEmail() ,getPass());
                Log.i("Checking credentials:  ", "Authenticate.isValid called");
                Authenticate.isValid();
            }
            else {
                RequestHandler.setHttpAuthentication();
                Log.i("Checking credentials:  ", "Authenticate.createAcc called");
                Authenticate.createAcc();
            }
        }
    }

    public void tosDialog(View v) {
        String url = "http://support.getcloudapp.com/customer/portal/articles/208750-terms-of-service";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    public static void startProgress(){
        progress.setMessage("Checking credentials...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
    }


    public void setCredentials(String email, String pass) {
        editPref = (ObscuredSharedPreferences.Editor) prefs.edit();
        editPref.putString("cred_userData", email);
        editPref.putString("cred_userPass", pass);
        editPref.apply();
    }

    public static String getEmail() {
        return prefs.getString("cred_userData", null);
    }
    public static String getPass() {
        return prefs.getString("cred_userPass", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();

        if(activityLoginState) {
            inflater.inflate(R.menu.authentication_menu_create, menu);
        } else {
            inflater.inflate(R.menu.authentication_menu_signin, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.register:
                if(activityLoginState) {
                    activityLoginState = false;
                    btn_toggle.setText(R.string.register);
                    invalidateOptionsMenu();
                }
                return true;

            case R.id.sign_in:
                if(!activityLoginState) {
                    activityLoginState = true;
                    btn_toggle.setText(R.string.sign_in);
                    invalidateOptionsMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}

package com.tevinjeffrey.vapor.ui.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.BuildConfig;
import com.tevinjeffrey.vapor.IntroActivity;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.VaporApp;
import com.tevinjeffrey.vapor.events.LoginEvent;
import com.tevinjeffrey.vapor.utils.VaporUtils;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity implements LoginView {

    @Bind(R.id.frameLayout)
    FrameLayout frameLayout;
    @Bind(R.id.login_field_email)
    EditText loginEmail;
    @Bind(R.id.login_wrapper_email)
    TextInputLayout loginWrapperEmail;
    @Bind(R.id.login_field_password)
    EditText loginPassword;
    @Bind(R.id.login_wrapper_password)
    TextInputLayout loginWrapperPassword;
    @Bind(R.id.login_button)
    Button loginButton;
    @Bind(R.id.login_field_container)
    RelativeLayout loginFieldContainer;
    @Bind(R.id.progressBar)
    ProgressBar loginProgressBar;

    @Inject
    LoginPresenter loginPresenter;

    @Inject
    Bus bus;

    View root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VaporApp.uiComponent(this).inject(this);
        root = LayoutInflater.from(this).inflate(R.layout.activity_login, null);
        setContentView(root);
        loginPresenter.attachView(this);
        ButterKnife.bind(this);
        bus.register(this);

        if (BuildConfig.DEBUG) {
            loginPassword.setText(BuildConfig.PASS);
            loginEmail.setText(BuildConfig.EMAIL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
        loginPresenter.detachView();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.login_button)
    public void onLoginClick(View view) {
        String userEmail = loginEmail.getText().toString();
        String userPass = loginPassword.getText().toString();

        if (isSignInFieldValid()) {
            showLoading(true);
            loginPresenter.tryLogin(userEmail, userPass);
        }
    }

    @Override
    public void showLoading(boolean loading) {
        loginProgressBar.setIndeterminate(loading);
        loginProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // previously visible view
                final View myView = loginFieldContainer;

                // get the center for the clipping circle
                int cx = (myView.getLeft() + myView.getRight()) / 2;
                int cy = (myView.getTop() + myView.getBottom()) / 2;

                // get the initial radius for the clipping circle
                int initialRadius = myView.getWidth();

                // create the animation (the final radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        myView.setVisibility(View.INVISIBLE);
                    }
                });

                // start the animation
                anim.start();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                // previously invisible view
                View myView = loginFieldContainer;

                // get the center for the clipping circle
                int cx = (myView.getLeft() + myView.getRight()) / 2;
                int cy = (myView.getTop() + myView.getBottom()) / 2;

                // get the final radius for the clipping circle
                int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

                // create the animator for this view (the start radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

                // make the view visible and start the animation
                myView.setVisibility(View.VISIBLE);
                anim.start();
            }
        }

    }

    @Override
    public void loginSuccessful(boolean isSuccessful) {
        if (isSuccessful) {
            bus.post(new LoginEvent());
            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void showError(Throwable t) {
        String message;
        Resources resources = getResources();

        if (t instanceof UnknownHostException) {
            message = resources.getString(R.string.no_internet);
        } else if (t instanceof SocketTimeoutException) {
            message = resources.getString(R.string.timed_out);
        } else {
            loginWrapperEmail.setErrorEnabled(true);
            loginWrapperPassword.setErrorEnabled(true);
            loginWrapperPassword.setError(getString(R.string.email_password_incorrect));
            Timber.e(t, "Error while logging in.");
            return;
        }
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }

    private boolean isSignInFieldValid() {

        String userEmail = loginEmail.getText().toString();
        String userPass = loginPassword.getText().toString();
        
        if (TextUtils.isEmpty(userEmail)) {
            loginWrapperEmail.setErrorEnabled(true);
            loginWrapperEmail.setError(getString(R.string.empty_email));
            return false;
        } else if (!VaporUtils.isValidEmail(userEmail)) {
            loginWrapperEmail.setErrorEnabled(true);
            loginWrapperEmail.setError(getString(R.string.invalid_email));
            return false;
        } else {
            loginWrapperEmail.setErrorEnabled(false);

        }
        if (TextUtils.isEmpty(userPass)) {
            loginWrapperPassword.setErrorEnabled(true);
            loginWrapperPassword.setError(getString(R.string.empty_password));
            return false;
        } else {
            loginWrapperPassword.setErrorEnabled(false);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @OnClick(R.id.tos)
    public void tosDialog(View v) {
        String url = "https://github.com/cloudapp/policy/blob/master/terms-of-service.md";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}

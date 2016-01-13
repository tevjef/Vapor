package com.tevinjeffrey.vapor.ui.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout

import com.squareup.otto.Bus
import com.tevinjeffrey.vapor.BuildConfig
import com.tevinjeffrey.vapor.R
import com.tevinjeffrey.vapor.VaporApp
import com.tevinjeffrey.vapor.ui.IntroActivity
import com.tevinjeffrey.vapor.utils.VaporUtils

import java.net.SocketTimeoutException
import java.net.UnknownHostException

import javax.inject.Inject

import butterknife.Bind
import butterknife.ButterKnife
import butterknife.OnClick
import timber.log.Timber

class LoginActivity : AppCompatActivity(), LoginView {

    var loginEmail: EditText? = null
    var loginWrapperEmail: TextInputLayout? = null
    var loginPassword: EditText? = null
    var loginWrapperPassword: TextInputLayout? = null
    var loginButton: Button? = null
    var loginFieldContainer: RelativeLayout? = null
    var loginProgressBar: ProgressBar? = null

    @Inject
    lateinit var loginPresenter: LoginPresenter

    @Inject
    lateinit var bus: Bus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VaporApp.uiComponent(this).inject(this)
        setContentView(R.layout.activity_login)
        loginPresenter.attachView(this)

        loginEmail = findViewById(R.id.login_field_email) as EditText
        loginWrapperEmail = findViewById(R.id.login_wrapper_email) as TextInputLayout
        loginPassword = findViewById(R.id.login_field_password) as EditText
        loginWrapperPassword = findViewById(R.id.login_wrapper_password) as TextInputLayout
        loginButton = findViewById(R.id.login_button) as Button
        loginFieldContainer = findViewById(R.id.login_field_container) as RelativeLayout
        loginProgressBar = findViewById(R.id.progressBar) as ProgressBar

        ButterKnife.bind(this)

        bus.register(this)

        if (BuildConfig.DEBUG) {
            loginPassword?.setText(BuildConfig.PASS)
            loginEmail?.setText(BuildConfig.EMAIL)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bus.unregister(this)
        loginPresenter.detachView()
        ButterKnife.unbind(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    @OnClick(R.id.login_button)
    fun onLoginClick(view: View) {
        val userEmail = loginEmail?.text.toString()
        val userPass = loginPassword?.text.toString()
        loginWrapperEmail?.isErrorEnabled = false
        loginWrapperPassword?.isErrorEnabled = false

        if (isSignInFieldValid) {
            showLoading(true)
            loginPresenter.tryLogin(userEmail, userPass)
        }
    }

    override fun showLoading(loading: Boolean) {
        loginProgressBar?.isIndeterminate = loading
        loginProgressBar?.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // previously visible view
                val myView: View = loginFieldContainer!!

                // get the center for the clipping circle
                val cx = (myView.left + myView.right) / 2
                val cy = (myView.top + myView.bottom) / 2

                // get the initial radius for the clipping circle
                val initialRadius = myView.width

                // create the animation (the final radius is zero)
                val anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius.toFloat(), 0f)

                // make the view invisible when the animation is done
                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        myView.visibility = View.INVISIBLE
                    }
                })

                // start the animation
                anim.start()
            } else {
                ViewCompat.animate(loginFieldContainer).alpha(0f).start()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                // previously invisible view
                val myView = loginFieldContainer!!

                // get the center for the clipping circle
                val cx = (myView.left + myView.right) / 2
                val cy = (myView.top + myView.bottom) / 2

                // get the final radius for the clipping circle
                val finalRadius = Math.max(myView.width, myView.height)

                // create the animator for this view (the start radius is zero)
                val anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0f, finalRadius.toFloat())

                // make the view visible and start the animation
                myView.visibility = View.VISIBLE
                anim.start()
            } else {
                ViewCompat.animate(loginFieldContainer).alpha(1f).start()
                /*
                loginFieldContainer.setVisibility(View.VISIBLE);
*/
            }
        }
    }

    override fun loginSuccessful() {
        val intent = Intent(this, IntroActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    override fun showError(t: Throwable) {
        val message: String?
        val resources = resources

        if (t is UnknownHostException) {
            message = resources.getString(R.string.no_internet)
        } else if (t is SocketTimeoutException) {
            message = resources.getString(R.string.timed_out)
        } else if (t is LoginException && t.code < 500) {
            loginWrapperEmail?.isErrorEnabled = true
            loginWrapperEmail?.error = " "
            loginWrapperPassword?.isErrorEnabled = true
            loginWrapperPassword?.error = getString(R.string.email_password_incorrect)
            Timber.e(t, "Error while logging in.")
            return
        } else {
            message = t.message
        }
        Snackbar.make(this.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    private val isSignInFieldValid: Boolean
        get() {

            val userEmail = loginEmail?.text.toString()
            val userPass = loginPassword?.text.toString()

            if (TextUtils.isEmpty(userEmail)) {
                loginWrapperEmail?.isErrorEnabled = true
                loginWrapperEmail?.error = getString(R.string.empty_email)
                return false
            } else if (!VaporUtils.isValidEmail(userEmail)) {
                loginWrapperEmail?.isErrorEnabled = true
                loginWrapperEmail?.error = getString(R.string.invalid_email)
                return false
            } else {
                loginWrapperEmail?.isErrorEnabled = false

            }
            if (TextUtils.isEmpty(userPass)) {
                loginWrapperPassword?.isErrorEnabled = true
                loginWrapperPassword?.error = getString(R.string.empty_password)
                return false
            } else {
                loginWrapperPassword?.isErrorEnabled = false
            }

            return true
        }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    @OnClick(R.id.tos)
    fun tosDialog(v: View) {
        val url = "https://github.com/cloudapp/policy/blob/master/terms-of-service.md"
        val i = Intent(Intent.ACTION_VIEW)
        i.setData(Uri.parse(url))
        startActivity(i)
    }
}

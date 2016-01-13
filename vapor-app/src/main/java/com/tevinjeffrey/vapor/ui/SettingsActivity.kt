package com.tevinjeffrey.vapor.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.ListView

import com.tevinjeffrey.vapor.R
import com.tevinjeffrey.vapor.utils.AppCompatPreferenceActivity

import butterknife.ButterKnife

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_preference)
        setToolbar()
        fragmentManager.beginTransaction().replace(R.id.content_frame, SettingsFragment()).commit()
    }

    private fun setToolbar() {
        val toolbar = ButterKnife.findById<Toolbar>(this, R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.title = getString(R.string.settings)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleStyle)
        toolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubtitleStyle_TextApperance)
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

    class SettingsFragment : PreferenceFragment() {

        private val parentActivity: SettingsActivity
            get() = activity as SettingsActivity

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //            getObjectGraph().inject(this);

            addPreferencesFromResource(R.xml.settings)
        }


        override fun onActivityCreated(savedInstanceState: Bundle?) {
            val list = view!!.findViewById(android.R.id.list) as ListView
            list.divider = ColorDrawable(Color.parseColor("#e6e6e6"))
            list.dividerHeight = parentActivity.resources.displayMetrics.density.toInt()

            super.onActivityCreated(savedInstanceState)
        }
    }
}

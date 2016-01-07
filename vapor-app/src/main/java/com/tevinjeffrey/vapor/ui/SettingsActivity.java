package com.tevinjeffrey.vapor.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.utils.AppCompatPreferenceActivity;

import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_preference);
        setToolbar();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }

    private void setToolbar() {
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.settings));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleStyle);
        toolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubtitleStyle_TextApperance);
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

    public static class SettingsFragment extends PreferenceFragment {

        private SettingsActivity getParentActivity() {
            return (SettingsActivity) getActivity();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
//            getObjectGraph().inject(this);

            addPreferencesFromResource(R.xml.settings);
        }



        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            ListView list = (ListView) getView().findViewById(android.R.id.list);
            list.setDivider(new ColorDrawable(Color.parseColor("#e6e6e6")));
            list.setDividerHeight((int) getParentActivity().getResources().getDisplayMetrics().density);

            super.onActivityCreated(savedInstanceState);
        }
    }
}

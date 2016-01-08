package com.tevinjeffrey.vapor.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.tevinjeffrey.vapor.R;

import timber.log.Timber;

public class IntroActivity extends AppIntro {
    @Override
    public void init(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            addSlide(AppIntroFragment.newInstance("Read Data", "Vapor needs your permission to upload and download files. Tap the arrow to grant.", R.drawable.save, ContextCompat.getColor(getApplicationContext(), R.color.primary)));
        }
        addSlide(AppIntroFragment.newInstance("Share Faster", "The fastest way to the upload and share your files. ", R.drawable.fast, ContextCompat.getColor(getApplicationContext(), R.color.primary)));
        addSlide(AppIntroFragment.newInstance("Share Anything", "From music to homework, CloudApp will create a sharable link for any file you want to share", R.drawable.files_devices, ContextCompat.getColor(getApplicationContext(), R.color.primary)));

        showSkipButton(false);
        setNavBarColor(R.color.primary_dark);
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @Override
    public void onSkipPressed() {
        finish();
    }

    @Override
    public void onNextPressed() {
    }

    @Override
    public void onDonePressed() {
        finish();
    }

    @Override
    public void onSlideChanged() {

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}

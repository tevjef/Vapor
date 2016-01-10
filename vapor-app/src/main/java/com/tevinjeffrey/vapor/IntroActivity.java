package com.tevinjeffrey.vapor;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import timber.log.Timber;

public class IntroActivity extends AppIntro {
    @Override
    public void init(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            addSlide(AppIntroFragment.newInstance("Read Data", "Vapor needs your permission to upload and download files. Tap the arrow to grant.", R.drawable.save, ContextCompat.getColor(getApplicationContext(), R.color.primary)));
        addSlide(AppIntroFragment.newInstance("Share Faster", "The fastest way to the upload and share your files. ", R.drawable.fast, ContextCompat.getColor(getApplicationContext(), R.color.primary)));
        addSlide(AppIntroFragment.newInstance("Share Anything", "From music to homework, CloudApp will create a sharable link for any file you want to share", R.drawable.files_devices, ContextCompat.getColor(getApplicationContext(), R.color.primary)));

        showSkipButton(false);
        setNavBarColor(R.color.primary_dark);
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        getPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Timber.d("onPageScrolled -- position=%s positionOffset=%s positionsOffsetPixels=%s", position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                Timber.d("onPageSelected -- position=%s", position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Timber.d("onPageScrollStateChanged -- state=%s", state);

            }
        });
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

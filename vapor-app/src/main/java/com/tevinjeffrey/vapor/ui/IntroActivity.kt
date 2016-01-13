package com.tevinjeffrey.vapor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat

import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.tevinjeffrey.vapor.R

class IntroActivity : AppIntro() {
    override fun init(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            addSlide(AppIntroFragment.newInstance("Read Data", "Vapor needs your permission to upload and download files. Tap the arrow to grant.", R.drawable.save, ContextCompat.getColor(applicationContext, R.color.primary)))
        }
        addSlide(AppIntroFragment.newInstance("Share Faster", "The fastest way to the upload and share your files. ", R.drawable.fast, ContextCompat.getColor(applicationContext, R.color.primary)))
        addSlide(AppIntroFragment.newInstance("Share Anything", "From music to homework, CloudApp will create a sharable link for any file you want to share", R.drawable.files_devices, ContextCompat.getColor(applicationContext, R.color.primary)))

        showSkipButton(false)
        setNavBarColor(R.color.primary_dark)
        askForPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1)
    }

    override fun onSkipPressed() {
        finish()
    }

    override fun onNextPressed() {
    }

    override fun onDonePressed() {
        finish()
    }

    override fun onSlideChanged() {

    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}

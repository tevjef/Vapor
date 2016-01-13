package com.tevinjeffrey.vapor.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView

import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.tevinjeffrey.vapor.R

import uk.co.senab.photoview.PhotoViewAttacher

class ImageActivity : Activity() {

    var imageView: ImageView? = null
    var mAttacher: PhotoViewAttacher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        super.onCreate(savedInstanceState)

        imageView = findViewById(R.id.imageView) as ImageView
        setContentView(R.layout.activity_image)

        val progress = MaterialDialog.Builder(this).content("Please wait...").progress(true, 0).show()

        Glide.with(applicationContext).load(intent.data).listener(object : RequestListener<Uri, GlideDrawable> {
            override fun onException(e: Exception, model: Uri, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: GlideDrawable, model: Uri, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                progress.dismiss()
                imageView?.setImageDrawable(resource)
                mAttacher = PhotoViewAttacher(imageView)
                return false
            }
        }).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView)

    }


    override fun onDestroy() {
        super.onDestroy()
    }
}

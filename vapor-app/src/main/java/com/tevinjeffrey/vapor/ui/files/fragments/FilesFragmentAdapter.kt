package com.tevinjeffrey.vapor.ui.files.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.tevinjeffrey.vapor.R
import com.tevinjeffrey.vapor.VaporApp
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem
import com.tevinjeffrey.vapor.ui.utils.ItemClickListener

import javax.inject.Inject

import butterknife.ButterKnife

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.ARCHIVE
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.AUDIO
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.BOOKMARK
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.IMAGE
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.TEXT
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.UNKNOWN
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.VIDEO

class FilesFragmentAdapter(private val cloudAppItems: List<CloudAppItem>, private val itemClickListener: ItemClickListener<CloudAppItem, View>) : RecyclerView.Adapter<FilesFragmentAdapter.FilesVH>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FilesVH {
        val parent = LayoutInflater.from(viewGroup.context).inflate(R.layout.fragment_files_grid_list_item, viewGroup, false)
        val filesVH = FilesVH.newInstance(parent)
        filesVH.setOnClickListener(View.OnClickListener { v ->
            val adapterPos = filesVH.adapterPosition
            if (adapterPos != RecyclerView.NO_POSITION) {
                itemClickListener.onItemClicked(cloudAppItems[adapterPos], v)
            }
        })
        return filesVH
    }

    override fun onBindViewHolder(holder: FilesVH, position: Int) {
        val cloudAppItem = cloudAppItems[position]
        holder.setItem(cloudAppItem)
    }

    override fun onViewRecycled(holder: FilesVH?) {
        super.onViewRecycled(holder)
        Glide.clear(holder!!.fileImage)
    }

    override fun getItemId(position: Int): Long {
        return cloudAppItems[position].itemId
    }

    override fun getItemCount(): Int {
        return cloudAppItems.size
    }

    class FilesVH(internal val parent: View, val fileImage: ImageView, internal val fileTitle: TextView, internal val fileTimeAgo: TextView) : RecyclerView.ViewHolder(parent) {

        @Inject
        lateinit var clipboardManager: ClipboardManager

        init {
            VaporApp.uiComponent(parent.context).inject(this)
        }

        fun setTitle(cloudAppItem: CloudAppItem) {
            fileTitle.text = cloudAppItem.name
        }

        fun setTimeAgo(cloudAppItem: CloudAppItem) {
            fileTimeAgo.text = DateUtils.getRelativeTimeSpanString(java.lang.Long.parseLong(cloudAppItem.createdAt), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
        }

        fun setImage(cloudAppItem: CloudAppItem) {
            val type = cloudAppItem.getItemType()
            var drawable: Drawable? = null

            val background = ButterKnife.findById<View>(parent, R.id.background_shade)
            val itemName = ButterKnife.findById<TextView>(parent, R.id.files_list_title)
            val timeSince = ButterKnife.findById<TextView>(parent, R.id.files_list_time_ago)
            val context = background.context

            itemName.setTextColor(Color.WHITE)
            timeSince.setTextColor(Color.WHITE)
            background.setBackgroundColor(ContextCompat.getColor(background.context, R.color.primary_dark))

            if (type === IMAGE && cloudAppItem.thumbnailUrl != null) {
                Glide.with(fileImage.context.applicationContext).load(cloudAppItem.thumbnailUrl).asBitmap().centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).listener(object : RequestListener<String, Bitmap> {
                    override fun onException(e: Exception, model: String, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap, model: String,
                                                 target: Target<Bitmap>,
                                                 isFromMemoryCache: Boolean,
                                                 isFirstResource: Boolean): Boolean {
                        Palette.from(resource).generate { palette ->
                            val ANIMATION_DURATION = 250
                            val swatch = palette.darkVibrantSwatch
                            if (swatch != null) {
                                itemName.setHasTransientState(true)
                                val animator = ObjectAnimator.ofInt(itemName,
                                        "textColor", itemName.currentTextColor,
                                        swatch.bodyTextColor).setDuration(ANIMATION_DURATION.toLong())
                                animator.setEvaluator(ArgbEvaluator())
                                animator.addListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        super.onAnimationEnd(animation)
                                        itemName.setHasTransientState(false)
                                    }
                                })
                                animator.start()

                                timeSince.setHasTransientState(true)
                                val animator1 = ObjectAnimator.ofInt(timeSince,
                                        "textColor", timeSince.currentTextColor,
                                        swatch.titleTextColor).setDuration(ANIMATION_DURATION.toLong())
                                animator1.setEvaluator(ArgbEvaluator())
                                animator1.addListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        super.onAnimationEnd(animation)
                                        timeSince.setHasTransientState(false)
                                    }
                                })
                                animator1.start()

                                background.setHasTransientState(true)
                                val animator3 = ObjectAnimator.ofInt(background,
                                        "backgroundColor", (background.background as ColorDrawable).color,
                                        ColorUtils.setAlphaComponent(swatch.rgb, 204))
                                animator3.setEvaluator(ArgbEvaluator())
                                animator3.addListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationStart(animation: Animator) {
                                        super.onAnimationStart(animation)
                                    }

                                    override fun onAnimationEnd(animation: Animator) {
                                        super.onAnimationEnd(animation)
                                        background.setHasTransientState(false)
                                    }
                                })
                                animator3.setDuration(ANIMATION_DURATION.toLong()).start()
                            }
                        }
                        return false
                    }
                }).into(fileImage)
                return
            } else if (type === IMAGE) {
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_photo)
            } else if (type === VIDEO) {
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_file_video)
            } else if (type === ARCHIVE) {
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_zip_box)
            } else if (type === BOOKMARK) {
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_bookmark)
            } else if (type === AUDIO) {
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_headset)
            } else if (type === TEXT) {
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_file_document)
            } else if (type === UNKNOWN) {
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_file_cloud)
            }

            fileImage.setImageDrawable(drawable)
        }


        fun setItem(item: CloudAppItem) {
            setImage(item)
            setTitle(item)
            setTimeAgo(item)
        }

        fun setOnClickListener(listener: View.OnClickListener) {
            parent.setOnClickListener(listener)
        }

        companion object {

            internal fun newInstance(parent: View): FilesVH {
                val imageView = ButterKnife.findById<ImageView>(parent, R.id.files_list_image)
                val fileTitle = ButterKnife.findById<TextView>(parent, R.id.files_list_title)
                val fileTimeAgo = ButterKnife.findById<TextView>(parent, R.id.files_list_time_ago)

                return FilesVH(parent, imageView, fileTitle, fileTimeAgo)
            }
        }
    }
}

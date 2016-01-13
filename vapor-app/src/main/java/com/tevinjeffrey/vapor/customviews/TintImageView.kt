package com.tevinjeffrey.vapor.customviews

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView

import com.tevinjeffrey.vapor.R


class TintImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ImageView(context, attrs, defStyleAttr) {
    @ColorInt
    private var color: Int = 0

    init {
        val a = getContext().obtainStyledAttributes(attrs, TINT_ATTRS,
                defStyleAttr, 0)

        color = fetchAccentColor()

        var drawable: Drawable
        if (a.length() > 0) {
            if (a.hasValue(2)) {
                color = a.getColor(2, 0)
            }
            if (a.hasValue(0)) {
                drawable = a.getDrawable(0)
                tintDrawable(drawable, color)
                setBackgroundDrawable(drawable)
            }
            if (a.hasValue(1)) {
                drawable = a.getDrawable(1)
                tintDrawable(drawable, color)
                setImageDrawable(drawable)
            }
        }
        a.recycle()
    }

    override fun setBackgroundDrawable(background: Drawable) {
        setBackgroundDrawable(background, color)
    }

    fun setBackgroundDrawable(background: Drawable, color: Int) {
        setImageDrawable(background, color)
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        val drawable = getDrawable(resId)
        setImageDrawable(drawable, color)
    }

    fun setImageResource(@DrawableRes resId: Int, @ColorInt color: Int) {
        val drawable = getDrawable(resId)
        setImageDrawable(drawable, color)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        setImageDrawable(drawable!!, color)
    }

    fun setImageDrawable(drawable: Drawable, @ColorInt color: Int) {
        tintDrawable(drawable, color)
        super.setImageDrawable(drawable)
    }

    private fun getDrawable(resId: Int): Drawable {
        return ContextCompat.getDrawable(context, resId)
    }

    fun tintDrawable(drawable: Drawable, @ColorInt color: Int): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrappedDrawable, color)
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN)
        return wrappedDrawable
    }

    private fun fetchAccentColor(): Int {
        var color = Color.DKGRAY
        try {
            val typedValue = TypedValue()
            val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
            color = a.getColor(0, 0)
            a.recycle()
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        return color
    }

    companion object {
        private val TINT_ATTRS = intArrayOf(android.R.attr.background, android.R.attr.src, android.R.attr.tint)
    }

}
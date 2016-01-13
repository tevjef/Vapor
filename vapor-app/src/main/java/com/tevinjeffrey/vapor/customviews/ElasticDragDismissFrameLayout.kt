/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tevinjeffrey.vapor.customviews

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.FrameLayout

import com.tevinjeffrey.vapor.R
import com.tevinjeffrey.vapor.utils.ColorUtils

import java.util.ArrayList


/**
 * A [FrameLayout] which responds to nested scrolls to create drag-dismissable layouts.
 * Applies an elasticity factor to reduce movement as you approach the given dismiss distance.
 * Optionally also scales down content during drag.
 */
class ElasticDragDismissFrameLayout : FrameLayout {

    // configurable attribs
    private var dragDismissDistance = java.lang.Float.MAX_VALUE
    private var dragDismissFraction = -1f
    private var dragDismissScale = 1f
    private var shouldScale = false
    private var dragElacticity = 0.8f

    // state
    private var totalDrag: Float = 0.toFloat()
    private var draggingDown = false
    private var draggingUp = false

    private var listeners: MutableList<ElasticDragDismissListener>? = null

    constructor(context: Context) : super(context, null) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0) {
    }

    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        val a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ElasticDragDismissFrameLayout, 0, 0)

        if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissDistance)) {
            dragDismissDistance = a.getDimensionPixelSize(R.styleable.ElasticDragDismissFrameLayout_dragDismissDistance, 0).toFloat()
        } else if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissFraction)) {
            dragDismissFraction = a.getFloat(R.styleable.ElasticDragDismissFrameLayout_dragDismissFraction, dragDismissFraction)
        }
        if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissScale)) {
            dragDismissScale = a.getFloat(R.styleable.ElasticDragDismissFrameLayout_dragDismissScale, dragDismissScale)
            shouldScale = dragDismissScale != 1f
        }
        if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragElasticity)) {
            dragElacticity = a.getFloat(R.styleable.ElasticDragDismissFrameLayout_dragElasticity,
                    dragElacticity)
        }
        a.recycle()
    }

    interface ElasticDragDismissListener {

        /**
         * Called for each drag event.

         * @param elasticOffset       Indicating the drag offset with elasticity applied i.e. may
         * *                            exceed 1.
         * *
         * @param elasticOffsetPixels The elastically scaled drag distance in pixels.
         * *
         * @param rawOffset           Value from [0, 1] indicating the raw drag offset i.e.
         * *                            without elasticity applied. A value of 1 indicates that the
         * *                            dismiss distance has been reached.
         * *
         * @param rawOffsetPixels     The raw distance the user has dragged
         */
        fun onDrag(elasticOffset: Float, elasticOffsetPixels: Float,
                   rawOffset: Float, rawOffsetPixels: Float)

        /**
         * Called when dragging is released and has exceeded the threshold dismiss distance.
         */
        fun onDragDismissed()

    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes and View.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        // if we're in a drag gesture and the user reverses up the we should take those events
        if (draggingDown && dy > 0 || draggingUp && dy < 0) {
            dragScale(dy)
            consumed[1] = dy
        }
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int,
                                dxUnconsumed: Int, dyUnconsumed: Int) {
        dragScale(dyUnconsumed)
    }

    override fun onStopNestedScroll(child: View) {
        if (Math.abs(totalDrag) >= dragDismissDistance) {
            dispatchDismissCallback()
        } else {
            // settle back to natural position
            animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(200L).setInterpolator(AnimationUtils.loadInterpolator(context, android.R.interpolator.fast_out_slow_in)).setListener(null).start()
            totalDrag = 0f
            draggingUp = false
            draggingDown = draggingUp
            dispatchDragCallback(0f, 0f, 0f, 0f)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (dragDismissFraction > 0f) {
            dragDismissDistance = h * dragDismissFraction
        }
    }

    fun addListener(listener: ElasticDragDismissListener) {
        if (listeners == null) {
            listeners = ArrayList<ElasticDragDismissListener>()
        }
        listeners!!.add(listener)
    }

    fun removeListener(listener: ElasticDragDismissListener) {
        if (listeners != null && listeners!!.size > 0) {
            listeners!!.remove(listener)
        }
    }

    private fun dragScale(scroll: Int) {
        if (scroll == 0) return

        totalDrag += scroll.toFloat()

        // track the direction & set the pivot point for scaling
        // don't double track i.e. if start dragging down and then reverse, keep tracking as
        // dragging down until they reach the 'natural' position
        if (scroll < 0 && !draggingUp && !draggingDown) {
            draggingDown = true
            if (shouldScale) pivotY = height.toFloat()
        } else if (scroll > 0 && !draggingDown && !draggingUp) {
            draggingUp = true
            if (shouldScale) pivotY = 0f
        }
        // how far have we dragged relative to the distance to perform a dismiss
        // (0–1 where 1 = dismiss distance). Decreasing logarithmically as we approach the limit
        var dragFraction = Math.log10((1 + Math.abs(totalDrag) / dragDismissDistance).toDouble()).toFloat()

        // calculate the desired translation given the drag fraction
        var dragTo = dragFraction * dragDismissDistance * dragElacticity

        if (draggingUp) {
            // as we use the absolute magnitude when calculating the drag fraction, need to
            // re-apply the drag direction
            dragTo *= -1f
        }
        translationY = dragTo

        if (shouldScale) {
            val scale = 1 - (1 - dragDismissScale) * dragFraction
            scaleX = scale
            scaleY = scale
        }

        // if we've reversed direction and gone past the settle point then clear the flags to
        // allow the list to get the scroll events & reset any transforms
        if (draggingDown && totalDrag >= 0 || draggingUp && totalDrag <= 0) {
            totalDrag = 0f
            dragTo = 0f
            dragFraction = 0f
            draggingDown = false
            draggingUp = false
            translationY = 0f
            scaleX = 1f
            scaleY = 1f
        }
        dispatchDragCallback(dragFraction, dragTo,
                Math.min(1f, Math.abs(totalDrag) / dragDismissDistance), totalDrag)
    }

    private fun dispatchDragCallback(elasticOffset: Float, elasticOffsetPixels: Float,
                                     rawOffset: Float, rawOffsetPixels: Float) {
        if (listeners != null && listeners!!.size > 0) {
            for (listener in listeners!!) {
                listener.onDrag(elasticOffset, elasticOffsetPixels,
                        rawOffset, rawOffsetPixels)
            }
        }
    }

    private fun dispatchDismissCallback() {
        if (listeners != null && listeners!!.size > 0) {
            for (listener in listeners!!) {
                listener.onDragDismissed()
            }
        }
    }

    /**
     * An [ElasticDragDismissListener] which fades system chrome (i.e. status bar and
     * navigation bar) when elastic drags are performed. Consuming classes must provide the
     * implementation for [ElasticDragDismissListener.onDragDismissed].
     */
    abstract class SystemChromeFader(private val window: Window) : ElasticDragDismissListener {

        override fun onDrag(elasticOffset: Float, elasticOffsetPixels: Float,
                            rawOffset: Float, rawOffsetPixels: Float) {
            if (elasticOffsetPixels < 0) {
                // dragging upward, fade the navigation bar in proportion
                // TODO don't fade nav bar on landscape phones?
                window.navigationBarColor = ColorUtils.modifyAlpha(window.navigationBarColor,
                        1f - rawOffset)
            } else if (elasticOffsetPixels == 0f) {
                // reset
                window.statusBarColor = ColorUtils.modifyAlpha(window.statusBarColor, 1f)
                window.navigationBarColor = ColorUtils.modifyAlpha(window.navigationBarColor, 1f)
            } else {
                // dragging downward, fade the status bar in proportion
                window.statusBarColor = ColorUtils.modifyAlpha(window.statusBarColor, 1f - rawOffset)
            }
        }

        abstract override fun onDragDismissed()
    }

}

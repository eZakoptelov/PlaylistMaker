package com.example.playlistmaker.player.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.toRect
import com.example.playlistmaker.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var playDrawable: Drawable? = null
    private var pauseDrawable: Drawable? = null

    private var isPlaying = false

    private val iconRect = RectF()

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView
        )

        try {
            val playDrawableId = typedArray.getResourceId(
                R.styleable.PlaybackButtonView_playIcon,
                0
            )

            val pauseDrawableId = typedArray.getResourceId(
                R.styleable.PlaybackButtonView_pauseIcon,
                0
            )

            if (playDrawableId != 0) {
                playDrawable = AppCompatResources.getDrawable(context, playDrawableId)
            }

            if (pauseDrawableId != 0) {
                pauseDrawable = AppCompatResources.getDrawable(context, pauseDrawableId)
            }
        } finally {
            typedArray.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val padding = 2f * resources.displayMetrics.density

        iconRect.set(
            padding,
            padding,
            w - padding,
            h - padding
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val icon = if (isPlaying) pauseDrawable else playDrawable

        icon?.let { drawable ->
            drawable.bounds = iconRect.toRect()
            drawable.draw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }

            MotionEvent.ACTION_UP -> {
                isPlaying = !isPlaying
                invalidate()
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                return true
            }
        }

        return super.onTouchEvent(event)
    }
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
    fun setPlaying(playing: Boolean) {
        isPlaying = playing
        invalidate()
    }
}
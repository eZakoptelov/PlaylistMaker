package com.example.playlistmaker.player.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.playlistmaker.R
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.createBitmap

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var playBitmap: Bitmap? = null
    private var pauseBitmap: Bitmap? = null

    private var isPlaying = false
    private val playRect = RectF()
    private val pauseRect = RectF()

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
                playBitmap = loadIcon(playDrawableId)

                if (playBitmap == null) {
                    Log.e("PlaybackButton", "Не удалось загрузить Play")
                }
            }

            if (pauseDrawableId != 0) {
                pauseBitmap = loadIcon(pauseDrawableId)

                if (pauseBitmap == null) {
                    Log.e("PlaybackButton", "Не удалось загрузить Pause")
                }
            }

        } finally {
            typedArray.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val padding = 2f * resources.displayMetrics.density

        playRect.set(
            padding,
            padding,
            w - padding,
            h - padding
        )

        pauseRect.set(
            padding,
            padding,
            w - padding,
            h - padding
        )
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isPlaying) {
            pauseBitmap?.let {
                canvas.drawBitmap(
                    it,
                    null,
                    pauseRect,
                    null
                )
            }
        } else {
            playBitmap?.let {
                canvas.drawBitmap(
                    it,
                    null,
                    playRect,
                    null
                )
            }
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
    private fun loadIcon(resourceId: Int): Bitmap? {

        val drawable = AppCompatResources.getDrawable(
            context,
            resourceId
        ) ?: return null

        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)

        val canvas = Canvas(bitmap)

        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        drawable.draw(canvas)

        return bitmap
    }
}
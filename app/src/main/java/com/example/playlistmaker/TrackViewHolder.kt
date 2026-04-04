package com.example.playlistmaker

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val artworkIv: ImageView = itemView.findViewById(R.id.artworkIv)
    private val trackNameTv: TextView = itemView.findViewById(R.id.trackNameTv)
    private val artistNameTv: TextView = itemView.findViewById(R.id.artistNameTv)
    private val trackTimeTv: TextView = itemView.findViewById(R.id.trackTimeTv)
    fun Int.dpToPx(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
    fun bind(track: Track) {
        val radiusInPx = 2.dpToPx(itemView.context)
        // Загрузка изображения с помощью Glide
        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .transforms(CenterCrop(), RoundedCorners(radiusInPx))
            .into(artworkIv)

        // Привязка текста
        trackNameTv.text = track.trackName
        artistNameTv.text = track.artistName
        trackTimeTv.text = track.trackTime
    }
}
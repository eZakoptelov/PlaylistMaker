package com.example.playlistmaker.presentation.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.TrackItem
import com.example.playlistmaker.presentation.utils.dpToPx
import okhttp3.internal.concurrent.formatDuration

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val artworkIv: ImageView = itemView.findViewById(R.id.artworkIv)
    private val trackNameTv: TextView = itemView.findViewById(R.id.trackNameTv)
    private val artistNameTv: TextView = itemView.findViewById(R.id.artistNameTv)
    private val trackTimeTv: TextView = itemView.findViewById(R.id.trackTimeTv)


    fun bind(track: TrackItem) {
        val imageUrl = track.artworkUrl100
        val radiusInPx = 2.dpToPx(itemView.context)

        // Загрузка изображения с помощью Glide
        Glide.with(itemView.context)
            .load(imageUrl.ifEmpty { null })
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .transform(CenterCrop(), RoundedCorners(radiusInPx))
            .into(artworkIv)

        // Привязка текста
        trackNameTv.text = track.trackName
        artistNameTv.text = track.artistName
        trackTimeTv.text = formatDuration(track.trackTimeMillis)
    }
}
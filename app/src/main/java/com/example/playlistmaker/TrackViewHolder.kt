package com.example.playlistmaker

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val artworkIv: ImageView = itemView.findViewById(R.id.artworkIv)
    private val trackNameTv: TextView = itemView.findViewById(R.id.trackNameTv)
    private val artistNameTv: TextView = itemView.findViewById(R.id.artistNameTv)
    private val trackTimeTv: TextView = itemView.findViewById(R.id.trackTimeTv)

    fun bind(track: Track) {
        // Загрузка изображения с помощью Glide
        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .fitCenter()
            .into(artworkIv)

        // Привязка текста
        trackNameTv.text = track.trackName
        artistNameTv.text = track.artistName
        trackTimeTv.text = track.trackTime
    }
}
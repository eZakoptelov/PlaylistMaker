package com.example.playlistmaker.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.TrackItem



class TrackAdapter(
    initialTracks: List<TrackItem>
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private var itemClickListener: OnItemClickListener? = null
    private val tracks = initialTracks.toMutableList()

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun submitList(newTracks: List<TrackItem>) {
        Log.d("ADAPTER", "submitList вызван, новых треков: ${newTracks.size}")
        tracks.clear()
        tracks.addAll(newTracks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        Log.d("MY_SEARCH", "Создан ViewHolder")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val trackName = itemView.findViewById<TextView>(R.id.trackNameTv)
        private val artistName = itemView.findViewById<TextView>(R.id.artistNameTv)
        private val duration = itemView.findViewById<TextView>(R.id.trackTimeTv)
        private val coverImage = itemView.findViewById<ImageView>(R.id.artworkIv)

        fun bind(track: TrackItem) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            duration.text = formatDuration(track.trackTimeMillis)

            Glide.with(itemView.context)
                .load(track.getCoverArtwork())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(coverImage)

            itemView.setOnClickListener {
                itemClickListener?.onItemClick(track)
            }
        }

        private fun formatDuration(millis: Long): String {
            val totalSeconds = millis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }
}

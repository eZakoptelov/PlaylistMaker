package com.example.playlistmaker

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(initialTracks: List<TrackItem>) : RecyclerView.Adapter<TrackViewHolder>() {

    private val tracks = initialTracks.toMutableList()

    // Метод для обновления списка
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
        Log.d("MY_SEARCH", "Привязан элемент $position")
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size
}
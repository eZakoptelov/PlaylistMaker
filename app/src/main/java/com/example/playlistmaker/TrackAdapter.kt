package com.example.playlistmaker

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


interface OnItemClickListener {
    fun onItemClick(track: TrackItem)
}

class TrackAdapter(initialTracks: List<TrackItem>) : RecyclerView.Adapter<TrackViewHolder>() {

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
        Log.d("MY_SEARCH", "Привязан элемент $position")
        holder.bind(tracks[position])

        holder.itemView.setOnClickListener {
            val selectedTrack = tracks[position]
            itemClickListener?.onItemClick(selectedTrack)
        }
    }

    override fun getItemCount(): Int = tracks.size
}


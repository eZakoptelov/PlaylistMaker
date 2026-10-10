package com.example.playlistmaker.playlists.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.databinding.ItemPlaylistBinding
import com.example.playlistmaker.playlists.domain.model.Playlist

class PlaylistAdapter(
    private val playlists: MutableList<Playlist> = mutableListOf()
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        with(holder.binding) {
            tvPlaylistName.text = playlist.name
            tvTrackCount.text = getTrackCountText(playlist.trackCount)

            val radius = (8 * root.context.resources.displayMetrics.density).toInt()
            if (playlist.coverPath.isNotEmpty()) {
                holder.binding.ivPlaceholder.visibility = View.GONE
                Glide.with(holder.itemView.context)
                    .load(playlist.coverPath)
                    .transform(CenterCrop(), RoundedCorners(radius))
                    .into(holder.binding.ivPlaylistCover)
            } else {
                holder.binding.ivPlaceholder.visibility = View.VISIBLE
                Glide.with(holder.itemView.context).clear(holder.binding.ivPlaylistCover)
            }
        }
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        notifyDataSetChanged()
    }

    private fun getTrackCountText(count: Int): String {
        val plural = when {
            count % 10 == 1 && count % 100 != 11 -> "трек"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "трека"
            else -> "треков"
        }
        return "$count $plural"
    }
}

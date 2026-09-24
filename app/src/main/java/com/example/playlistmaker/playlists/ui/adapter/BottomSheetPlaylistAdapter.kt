package com.example.playlistmaker.playlists.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistBottomSheetBinding
import com.example.playlistmaker.playlists.domain.model.Playlist

class BottomSheetPlaylistAdapter(
    private val playlists: MutableList<Playlist> = mutableListOf(),
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<BottomSheetPlaylistAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPlaylistBottomSheetBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaylistBottomSheetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        with(holder.binding) {
            tvPlaylistName.text = playlist.name
            tvTrackCount.text = getTrackCountText(playlist.trackCount)

            val radius = (8 * root.context.resources.displayMetrics.density).toInt()

            if (playlist.coverPath.isNotEmpty()) {
                Glide.with(root.context)
                    .load(playlist.coverPath)
                    .transform(CenterCrop(), RoundedCorners(radius))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivPlaylistCover)
            } else {
                Glide.with(root.context)
                    .load(R.drawable.ic_placeholder)
                    .transform(CenterCrop(), RoundedCorners(radius))
                    .into(ivPlaylistCover)
            }


            root.setOnClickListener { onClick(playlist) }
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

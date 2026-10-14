package com.example.playlistmaker.playlists.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistBinding
import com.example.playlistmaker.playlists.domain.model.Playlist

class PlaylistAdapter(
    private val onItemClick: (Long) -> Unit,
    private val playlists: MutableList<Playlist> = mutableListOf()
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(val binding: ItemPlaylistBinding) :
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

        holder.itemView.setOnClickListener {
            onItemClick(playlist.id)
        }

        with(holder.binding) {
            tvPlaylistName.text = playlist.name
            tvTrackCount.text = root.context.resources.getQuantityString(
                R.plurals.playlist_track_count,
                playlist.trackCount,
                playlist.trackCount
            )

            val radius = (8 * root.context.resources.displayMetrics.density).toInt()
            if (playlist.coverPath.isNotEmpty()) {
                ivPlaceholder.visibility = View.GONE
                Glide.with(holder.itemView.context)
                    .load(playlist.coverPath)
                    .transform(CenterCrop(), RoundedCorners(radius))
                    .into(ivPlaylistCover)
            } else {
                ivPlaceholder.visibility = View.VISIBLE
                Glide.with(holder.itemView.context).clear(ivPlaylistCover)
            }
        }
    }

    override fun getItemCount(): Int = playlists.size


    fun updatePlaylists(newPlaylists: List<Playlist>) {
        val diffResult = DiffUtil.calculateDiff(
            PlaylistDiffCallback(playlists.toList(), newPlaylists)
        )

        playlists.clear()
        playlists.addAll(newPlaylists)

        diffResult.dispatchUpdatesTo(this)
    }

    private class PlaylistDiffCallback(
        private val oldList: List<Playlist>,
        private val newList: List<Playlist>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldList[oldItemPosition].id ==
                    newList[newItemPosition].id
        }

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldList[oldItemPosition] ==
                    newList[newItemPosition]
        }
    }
}

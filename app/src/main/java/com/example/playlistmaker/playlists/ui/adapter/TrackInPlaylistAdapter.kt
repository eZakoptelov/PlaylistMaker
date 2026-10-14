package com.example.playlistmaker.playlists.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemTrackBinding
import com.example.playlistmaker.search.domain.model.TrackItem

class TrackInPlaylistAdapter(
    private val onItemClick: (TrackItem) -> Unit,
    private val onItemLongClick: (TrackItem) -> Unit
) : ListAdapter<TrackItem, TrackInPlaylistAdapter.TrackViewHolder>(TrackDiffCallback()) {

    inner class TrackViewHolder(val binding: ItemTrackBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)


        holder.binding.trackNameTv.text = track.trackName
        holder.binding.artistNameTv.text = track.artistName
        val minutes = (track.trackTimeMillis / 1000) / 60
        val seconds = (track.trackTimeMillis / 1000) % 60

        holder.binding.trackTimeTv.text = String.format(
            java.util.Locale.getDefault(),
            "%d:%02d",
            minutes,
            seconds
        )

        val coverUrl = track.getCoverArtwork()
        if (coverUrl.isBlank()) {
            holder.binding.artworkIv.setImageResource(R.drawable.ic_placeholder)
        } else {
            Glide.with(holder.itemView.context)
                .load(coverUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.binding.artworkIv)
        }
    }

    class TrackDiffCallback : DiffUtil.ItemCallback<TrackItem>() {
        override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean =
            oldItem.trackId == newItem.trackId

        override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean =
            oldItem == newItem
    }
}

package com.example.playlistmaker.playlists.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
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

    class ViewHolder(val binding: ItemPlaylistBottomSheetBinding) :
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
            tvTrackCount.text = root.context.resources.getQuantityString(
                R.plurals.playlist_track_count,
                playlist.trackCount,
                playlist.trackCount
            )

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

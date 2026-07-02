package com.example.playlistmaker.search.ui.adapter

import com.example.playlistmaker.search.domain.model.TrackItem

interface OnItemClickListener {
    fun onItemClick(track: TrackItem)
}
package com.example.playlistmaker.presentation.adapter

import com.example.playlistmaker.domain.model.TrackItem

interface OnItemClickListener {
    fun onItemClick(track: TrackItem)
}
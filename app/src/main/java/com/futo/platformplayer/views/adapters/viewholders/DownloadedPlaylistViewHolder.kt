package com.futo.platformplayer.views.adapters.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.futo.platformplayer.R
import com.futo.platformplayer.images.GlideHelper.Companion.crossfade
import com.futo.platformplayer.models.PlaylistDownloadedOrWatchlist
import com.futo.platformplayer.states.StatePlaylists
import com.futo.platformplayer.views.adapters.AnyAdapter

class DownloadedPlaylistViewHolder(view: ViewGroup) : AnyAdapter.AnyViewHolder<PlaylistDownloadedOrWatchlist>(
    LayoutInflater.from(view.context).inflate(R.layout.list_downloaded_playlist, view, false)
) {
    var imageView: ImageView = _view.findViewById(R.id.downloaded_playlist_image);
    var imageText: TextView = _view.findViewById(R.id.downloaded_playlist_name);

    override fun bind(value: PlaylistDownloadedOrWatchlist) {
        imageText.text = if (value.isWatchlist) { _view.context.getString(R.string.watch_later) } else { value.playlist!!.name }
        val firstVideo = if (value.isWatchlist) { StatePlaylists.instance.getWatchLater() } else { value.videos }.firstOrNull()
        Glide.with(imageView)
            .load(firstVideo?.thumbnails)
            .crossfade()
            .into(imageView);
    }

}
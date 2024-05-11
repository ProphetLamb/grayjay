package com.futo.platformplayer.views.adapters

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.futo.platformplayer.R
import com.futo.platformplayer.api.media.structures.TemporalItem
import com.futo.platformplayer.fragment.mainactivity.main.DownloadsFragment
import com.futo.platformplayer.models.PlaylistDownloadedOrWatchlist
import com.futo.platformplayer.states.StateDownloads
import com.futo.platformplayer.states.StatePlaylists
import com.futo.platformplayer.views.adapters.viewholders.DownloadedPlaylistViewHolder

class DownloadedPlaylistAdapter(frag: DownloadsFragment, view: View) : DownloadFilterableAdapter<PlaylistDownloadedOrWatchlist, DownloadedPlaylistViewHolder>(frag, view)  {
    private val _listPlaylistsContainer: LinearLayout = view.findViewById(R.id.downloads_playlist_container)
    private val _meta: TextView = view.findViewById(R.id.downloads_playlist_meta)
    private val _listPlaylists: LinearLayout = view.findViewById(R.id.downloads_playlist_list)

    init {
        StateDownloads.instance.onDownloadedChanged.subscribe {
            updateDataset()
            updateContainer()
        }
        updateDataset()
        updateContainer()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadedPlaylistViewHolder {
        val holder = DownloadedPlaylistViewHolder(parent)
        return holder
    }

    override fun updateContainer() {
        _listPlaylistsContainer.visibility = if (itemCount == 0) { GONE } else { VISIBLE }
        val countText = if (sourceCount == itemCount) "${sourceCount}" else "${sourceCount} / ${itemCount}"
        _meta.text = "(${countText} ${_view.context.getString(R.string.playlists).lowercase()}, ${_filteredDataset.sumOf { it.videos.size }} ${_view.context.getString(R.string.videos).lowercase()})"
    }

    override fun updateDataset() {
         var playlistTemporal = StateDownloads.instance.getDownloadPlaylistsTemporal().mapNotNull {
            val playlist = StatePlaylists.instance.getPlaylist(it.inner.id)
            if (playlist == null) {
                null
            } else {
                TemporalItem(PlaylistDownloadedOrWatchlist.create(it.inner, playlist), it.createdAt)
            }
        } + listOfNotNull(StateDownloads.instance.getWatchLaterDescriptor()).map {
            val playlist = StatePlaylists.instance.getWatchLater()
            TemporalItem.now(PlaylistDownloadedOrWatchlist.watchlist(it, playlist, _view.context))
        }
        _originalDataset = playlistTemporal

        if (!query.isNullOrBlank()) {
            val query = query!!.trim()
            playlistTemporal = playlistTemporal
                .filter { it.inner.name.contains(query, true) }
        }
        _filteredDataset = when (sortBy) {
            0 -> playlistTemporal.sortedByDescending { it.createdAt }
            1 -> playlistTemporal.sortedBy { it.createdAt }
            2 -> playlistTemporal.sortedBy { it.inner.name }
            3 -> playlistTemporal.sortedByDescending { it.inner.name }
            else -> throw IllegalStateException("Invalid sorting algorithm selected.")
        }.map { it.inner }

        notifyDataSetChanged()
    }
}
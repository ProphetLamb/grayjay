package com.futo.platformplayer.models

import android.content.Context
import com.futo.platformplayer.R
import com.futo.platformplayer.api.media.models.video.SerializedPlatformVideo
import com.futo.platformplayer.downloads.PlaylistDownloadDescriptor
import com.futo.platformplayer.downloads.VideoDownload

class PlaylistDownloadedOrWatchlist(
    val downloadDescriptor: PlaylistDownloadDescriptor,
    val videos: List<SerializedPlatformVideo>,
    val name: String,
    val playlist: Playlist?,
) {
    val isWatchlist: Boolean
        get() { return playlist == null || downloadDescriptor.id == VideoDownload.GROUP_WATCHLATER }

    companion object {
        fun create(downloadDescriptor: PlaylistDownloadDescriptor, playlist: Playlist): PlaylistDownloadedOrWatchlist {
            return PlaylistDownloadedOrWatchlist(
                downloadDescriptor,
                playlist.videos,
                playlist.name,
                playlist
            );
        }
        fun watchlist(
            downloadDescriptor: PlaylistDownloadDescriptor,
            videos: List<SerializedPlatformVideo>,
            context: Context
        ): PlaylistDownloadedOrWatchlist {
            return PlaylistDownloadedOrWatchlist(
                downloadDescriptor,
                videos,
                context.getString(R.string.watch_later),
                null
            );
        }
    }

}
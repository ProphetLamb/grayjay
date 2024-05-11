package com.futo.platformplayer.views.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.futo.platformplayer.R
import com.futo.platformplayer.downloads.VideoDownload
import com.futo.platformplayer.downloads.VideoLocal
import com.futo.platformplayer.fragment.mainactivity.main.DownloadsFragment
import com.futo.platformplayer.fragment.mainactivity.main.VideoDetailFragment
import com.futo.platformplayer.states.StateDownloads
import com.futo.platformplayer.states.StatePlayer
import com.futo.platformplayer.views.adapters.viewholders.DownloadedVideoViewHolder

class DownloadedVideoAdapter(frag: DownloadsFragment, view: View) :
    DownloadFilterableAdapter<VideoLocal, DownloadedVideoViewHolder>(frag, view) {
    private val _containerHeader: LinearLayout
    private val _containerSortBy: LinearLayout
    private val _containerSearch: FrameLayout
    private val _meta: TextView

    init {
        _containerHeader = _view.findViewById(R.id.downloads_videos_header)
        _containerSortBy = _view.findViewById(R.id.sortby_container)
        _containerSearch = _view.findViewById(R.id.container_search)
        _meta = _view.findViewById(R.id.downloads_videos_meta)

        StateDownloads.instance.onDownloadedChanged.subscribe {
            updateDataset()
            updateContainer()
        }
        updateDataset()
        updateContainer()

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadedVideoViewHolder {
        val holder = DownloadedVideoViewHolder(parent)
        holder.onClick.subscribe {
            StatePlayer.instance.clearQueue()
            _frag.navigate<VideoDetailFragment>(it).maximizeVideoDetail()
        }
        return holder
    }

    override fun onBindViewHolder(holder: DownloadedVideoViewHolder, position: Int) {
        holder.bind(_filteredDataset[position])
    }
    
    override fun updateContainer() {
        if(sourceCount == 0) {
            _containerHeader.visibility = LinearLayout.GONE
            _containerSearch.visibility = LinearLayout.GONE
            _containerSortBy.visibility = LinearLayout.GONE
        } else {
            _containerHeader.visibility = LinearLayout.VISIBLE
            _containerSearch.visibility = LinearLayout.VISIBLE
            _containerSortBy.visibility = LinearLayout.VISIBLE

            val countText = if (sourceCount == itemCount) "${sourceCount}" else "${sourceCount} / ${itemCount}"
            _meta.text = "(${countText} ${_view.context.getString(R.string.videos).lowercase()})"
        }
    }

    override fun updateDataset() {
        var downloadedTemporal = StateDownloads.instance.getDownloadedVideosTemporal()
            .filter { it.inner.groupType != VideoDownload.GROUP_PLAYLIST || it.inner.groupID == null || !StateDownloads.instance.hasCachedPlaylist(it.inner.groupID!!) }
        _originalDataset = downloadedTemporal
        if (!query.isNullOrBlank()) {
            val query = query!!.trim()
            downloadedTemporal = downloadedTemporal
                .filter { it.inner.name.contains(query, true)
                    || it.inner.description.contains(query, true)
                    || it.inner.author.name.contains(query, true)  }
        }
        _filteredDataset = when (sortBy) {
            0 -> downloadedTemporal.sortedByDescending { it.createdAt }
            1 -> downloadedTemporal.sortedBy { it.createdAt }
            2 -> downloadedTemporal.sortedBy { it.inner.name }
            3 -> downloadedTemporal.sortedByDescending { it.inner.name }
            else -> throw IllegalStateException("Invalid sorting algorithm selected.")
        }.map { it.inner }

        notifyDataSetChanged()
    }
}
package com.futo.platformplayer.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.futo.platformplayer.api.media.structures.TemporalItem
import com.futo.platformplayer.downloads.VideoDownload
import com.futo.platformplayer.downloads.VideoLocal
import com.futo.platformplayer.fragment.mainactivity.main.DownloadsFragment
import com.futo.platformplayer.fragment.mainactivity.main.VideoDetailFragment
import com.futo.platformplayer.states.StateDownloads
import com.futo.platformplayer.states.StatePlayer
import com.futo.platformplayer.views.adapters.viewholders.VideoDownloadViewHolder

class VideoDownloadAdapter(frag: DownloadsFragment, inflater: LayoutInflater) :
    RecyclerView.Adapter<VideoDownloadViewHolder>() {
    private lateinit var _filteredDataset: List<VideoLocal>
    private lateinit var _originalDataset: List<TemporalItem<VideoLocal>>
    private val _inflater: LayoutInflater = inflater
    private val _frag: DownloadsFragment = frag

    var sortBy = 0
        set(value) { field = value; updateDataset() };
    var query: String? = null
        set(value) { field = value; updateDataset() };

    init {
        StateDownloads.instance.onDownloadedChanged.subscribe { updateDataset(); }
        updateDataset()
    }
    override fun getItemCount() = _filteredDataset.size;

    val sourceCount get() = _originalDataset.size;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoDownloadViewHolder {
        val holder = VideoDownloadViewHolder(parent);
        holder.onClick.subscribe {
            StatePlayer.instance.clearQueue();
            _frag.navigate<VideoDetailFragment>(it).maximizeVideoDetail();
        }
        return holder;
    }

    override fun onBindViewHolder(holder: VideoDownloadViewHolder, position: Int) {
        holder.bind(_filteredDataset[position])
    }

    private fun updateDataset() {
        var downloadedTemporal = StateDownloads.instance.getDownloadedVideosTemporal()
            .filter { it.inner.groupType != VideoDownload.GROUP_PLAYLIST || it.inner.groupID == null || !StateDownloads.instance.hasCachedPlaylist(it.inner.groupID!!) };
        _originalDataset = downloadedTemporal;
        if (!query.isNullOrBlank()) {
            val query = query!!.trim();
            downloadedTemporal = downloadedTemporal
                .filter { it.inner.name.contains(query, true)
                    || it.inner.description.contains(query, true)
                    || it.inner.author.name.contains(query, true)  };
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
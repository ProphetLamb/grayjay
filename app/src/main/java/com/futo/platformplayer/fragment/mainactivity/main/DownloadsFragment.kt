package com.futo.platformplayer.fragment.mainactivity.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.futo.platformplayer.R
import com.futo.platformplayer.logging.Logger
import com.futo.platformplayer.models.Playlist
import com.futo.platformplayer.states.StateDownloads
import com.futo.platformplayer.states.StatePlaylists
import com.futo.platformplayer.toHumanBytesSize
import com.futo.platformplayer.views.adapters.VideoDownloadAdapter
import com.futo.platformplayer.views.items.ActiveDownloadItem
import com.futo.platformplayer.views.items.PlaylistDownloadItem
import com.futo.platformplayer.views.others.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadsFragment : MainFragment() {
    private val TAG = "DownloadsFragment";

    override val isMainView : Boolean = true;
    override val isTab: Boolean = true;
    override val hasBottomBar: Boolean get() = true;

    private var _view: DownloadsView? = null;

    override fun onCreateMainView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = DownloadsView(this, inflater);
        _view = view;
        return view;
    }

    override fun onResume() {
        super.onResume()
        _view?.reloadUI();

        val reloadUiScoped = {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    Logger.i(TAG, "Reloading UI for downloads");
                    _view?.reloadUI()
                } catch (e: Throwable) {
                    Logger.e(TAG, "Failed to reload UI for downloads", e)
                }
            }
        }

        StateDownloads.instance.onDownloadsChanged.subscribe(this) { reloadUiScoped() };
        StateDownloads.instance.onDownloadedChanged.subscribe(this) { reloadUiScoped() };
        StateDownloads.instance.onExportsChanged.subscribe(this) { reloadUiScoped() };
    }

    override fun onPause() {
        super.onPause();

        StateDownloads.instance.onDownloadsChanged.remove(this);
        StateDownloads.instance.onDownloadedChanged.remove(this);
        StateDownloads.instance.onExportsChanged.remove(this);
    }

    private class DownloadsView(frag: DownloadsFragment, inflater: LayoutInflater) :
        LinearLayout(frag.requireContext()) {
        private val TAG = "DownloadsView";
        private val _frag: DownloadsFragment = frag;

        private val _usageUsed: TextView;
        private val _usageAvailable: TextView;
        private val _usageProgress: ProgressBar;

        private val _listActiveDownloadsContainer: LinearLayout;
        private val _listActiveDownloadsMeta: TextView;
        private val _listActiveDownloads: LinearLayout;

        private val _listPlaylistsContainer: LinearLayout;
        private val _listPlaylistsMeta: TextView;
        private val _listPlaylists: LinearLayout;

        private val _listDownloadedHeader: LinearLayout;
        private val _listDownloadedMeta: TextView;
        private val _listDownloadsFilterContainer: LinearLayout
        private val _listDownloaded: VideoDownloadAdapter

        init {
            inflater.inflate(R.layout.fragment_downloads, this, true)
            _usageUsed = findViewById(R.id.downloads_usage_used)
            _usageAvailable = findViewById(R.id.downloads_usage_available)
            _usageProgress = findViewById(R.id.downloads_usage_progress)
            _listActiveDownloadsContainer = findViewById(R.id.downloads_active_downloads_container)
            _listActiveDownloadsMeta = findViewById(R.id.downloads_active_downloads_meta)
            _listActiveDownloads = findViewById(R.id.downloads_active_downloads_list)
            _listPlaylistsContainer = findViewById(R.id.downloads_playlist_container)
            _listPlaylistsMeta = findViewById(R.id.downloads_playlist_meta)
            _listPlaylists = findViewById(R.id.downloads_playlist_list)
            _listDownloadedHeader = findViewById(R.id.downloads_videos_header)
            _listDownloadedMeta = findViewById(R.id.downloads_videos_meta)
            _listDownloadsFilterContainer = findViewById(R.id.downloads_videos_filter_container)
            _listDownloaded = VideoDownloadAdapter(_frag, inflater)
            val spinnerSortBy: Spinner = findViewById(R.id.spinner_sortby)
            spinnerSortBy.adapter = ArrayAdapter(context, R.layout.spinner_item_simple, resources.getStringArray(R.array.downloads_sortby_array)).also {
                it.setDropDownViewResource(R.layout.spinner_dropdownitem_simple)
            }
            spinnerSortBy.setSelection(_listDownloaded.sortBy)
            spinnerSortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    _listDownloaded.sortBy = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
            val listDownloadsView = findViewById<RecyclerView>(R.id.list_downloaded);
            listDownloadsView.adapter = _listDownloaded
            listDownloadsView.layoutManager = LinearLayoutManager(context)
            reloadUI()
        }

        fun reloadUI() {
            val usage = StateDownloads.instance.getTotalUsage(true);
            _usageUsed.text = "${usage.usage.toHumanBytesSize()} " + context.getString(R.string.used);
            _usageAvailable.text = "${usage.available.toHumanBytesSize()} " + context.getString(R.string.available);
            _usageProgress.progress = usage.percentage.toFloat();

            val activeDownloads = StateDownloads.instance.getDownloading();
            val playlists = StateDownloads.instance.getCachedPlaylists();
            val watchLaterDownload = StateDownloads.instance.getWatchLaterDescriptor();

            if(activeDownloads.isEmpty())
                _listActiveDownloadsContainer.visibility = GONE;
            else {
                _listActiveDownloadsContainer.visibility = VISIBLE;
                _listActiveDownloadsMeta.text = "(${activeDownloads.size} videos)";

                _listActiveDownloads.removeAllViews();
                for(view in activeDownloads.take(4).map { ActiveDownloadItem(context, it, _frag.lifecycleScope) })
                    _listActiveDownloads.addView(view);
            }

            if(playlists.isEmpty() && watchLaterDownload == null)
                _listPlaylistsContainer.visibility = GONE;
            else {
                _listPlaylistsContainer.visibility = VISIBLE;

                val watchLater = if(watchLaterDownload != null) StatePlaylists.instance.getWatchLater() else listOf();

                _listPlaylistsMeta.text = "(${playlists.size + (if(watchLaterDownload != null) 1 else 0)} ${context.getString(R.string.playlists).lowercase()}, ${playlists.sumOf { it.playlist.videos.size } + watchLater.size} ${context.getString(R.string.videos).lowercase()})";

                _listPlaylists.removeAllViews();
                if(watchLaterDownload != null) {
                    val pdView = PlaylistDownloadItem(context, "Watch Later", watchLater.firstOrNull()?.thumbnails?.getHQThumbnail(), "WATCHLATER");
                    pdView.setOnClickListener {
                        _frag.navigate<WatchLaterFragment>();
                    }
                    _listPlaylists.addView(pdView);
                }
                for(view in playlists.map { PlaylistDownloadItem(context, it.playlist.name, it.playlist.videos.firstOrNull()?.thumbnails?.getHQThumbnail(), it.playlist) }) {
                    view.setOnClickListener {
                        if(view.obj is Playlist) {
                            _frag.navigate<PlaylistFragment>(view.obj);
                        }
                    };
                    _listPlaylists.addView(view);
                }
            }

            if(_listDownloaded.itemCount == 0) {
                _listDownloadedHeader.visibility = GONE;
                _listDownloadsFilterContainer.visibility = GONE;
            } else {
                _listDownloadedHeader.visibility = VISIBLE;
                _listDownloadsFilterContainer.visibility = VISIBLE;

                val countText = if (_listDownloaded.sourceCount == _listDownloaded.itemCount) "${_listDownloaded.sourceCount}" else "${_listDownloaded.sourceCount} / ${_listDownloaded.itemCount}";
                _listDownloadedMeta.text = "(${countText} ${context.getString(R.string.videos).lowercase()})";
            }
        }
    }
}
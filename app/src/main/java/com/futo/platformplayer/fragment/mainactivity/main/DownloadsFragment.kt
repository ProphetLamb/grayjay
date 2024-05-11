package com.futo.platformplayer.fragment.mainactivity.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.futo.platformplayer.R
import com.futo.platformplayer.logging.Logger
import com.futo.platformplayer.states.StateDownloads
import com.futo.platformplayer.toHumanBytesSize
import com.futo.platformplayer.views.adapters.DownloadedPlaylistAdapter
import com.futo.platformplayer.views.adapters.DownloadedVideoAdapter
import com.futo.platformplayer.views.items.ActiveDownloadItem
import com.futo.platformplayer.views.others.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadsFragment : MainFragment() {
    private val TAG = "DownloadsFragment"

    override val isMainView : Boolean = true
    override val isTab: Boolean = true
    override val hasBottomBar: Boolean get() = true

    private var _view: DownloadsView? = null

    override fun onCreateMainView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = DownloadsView(this, inflater)
        _view = view
        return view
    }

    override fun onResume() {
        super.onResume()
        _view?.reloadUI()

        val reloadUiScoped = {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    Logger.i(TAG, "Reloading UI for downloads")
                    _view?.reloadUI()
                } catch (e: Throwable) {
                    Logger.e(TAG, "Failed to reload UI for downloads", e)
                }
            }
        }

        StateDownloads.instance.onDownloadsChanged.subscribe(this) { reloadUiScoped() }
        StateDownloads.instance.onExportsChanged.subscribe(this) { reloadUiScoped() }
    }

    override fun onPause() {
        super.onPause()

        StateDownloads.instance.onDownloadsChanged.remove(this)
        StateDownloads.instance.onExportsChanged.remove(this)
    }

    private class DownloadsView(
        private val _frag: DownloadsFragment,
        inflater: LayoutInflater
    ) : LinearLayout(_frag.requireContext()) {
        private val TAG = "DownloadsView"

        init {
            inflater.inflate(R.layout.fragment_downloads, this, true)
        }

        private val _clearSearch: ImageButton = findViewById(R.id.button_clear_search)
        private val _editSearch: EditText = findViewById(R.id.edit_search)

        private val _usageUsed: TextView = findViewById(R.id.downloads_usage_used)
        private val _usageAvailable: TextView = findViewById(R.id.downloads_usage_available)
        private val _usageProgress: ProgressBar = findViewById(R.id.downloads_usage_progress)

        private val _listActiveDownloadsContainer: LinearLayout = findViewById(R.id.downloads_active_downloads_container)
        private val _listActiveDownloadsMeta: TextView = findViewById(R.id.downloads_active_downloads_meta)
        private val _listActiveDownloads: LinearLayout = findViewById(R.id.downloads_active_downloads_list)

        private val _listPlaylists: DownloadedPlaylistAdapter = DownloadedPlaylistAdapter(_frag, this)
        private val _listDownloaded: DownloadedVideoAdapter = DownloadedVideoAdapter(_frag, this)

        init {
            val listPlaylistsView = findViewById<RecyclerView>(R.id.downloads_playlist_list)
            listPlaylistsView.adapter = _listPlaylists
            listPlaylistsView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val listDownloadsView = findViewById<RecyclerView>(R.id.list_downloaded)
            listDownloadsView.adapter = _listDownloaded
            listDownloadsView.layoutManager = LinearLayoutManager(context)
            reloadUI()

            _editSearch.addTextChangedListener {
                _clearSearch.visibility = if (it.isNullOrBlank()) View.GONE else View.VISIBLE
            }
            _clearSearch.setOnClickListener {
                _editSearch.text.clear()
            }
        }

        fun reloadUI() {
            val usage = StateDownloads.instance.getTotalUsage(true)
            _usageUsed.text = "${usage.usage.toHumanBytesSize()} " + context.getString(R.string.used)
            _usageAvailable.text = "${usage.available.toHumanBytesSize()} " + context.getString(R.string.available)
            _usageProgress.progress = usage.percentage.toFloat()

            val activeDownloads = StateDownloads.instance.getDownloading()

            if(activeDownloads.isEmpty())
                _listActiveDownloadsContainer.visibility = GONE
            else {
                _listActiveDownloadsContainer.visibility = VISIBLE
                _listActiveDownloadsMeta.text = "(${activeDownloads.size} videos)"

                _listActiveDownloads.removeAllViews()
                for(view in activeDownloads.take(4).map { ActiveDownloadItem(context, it, _frag.lifecycleScope) })
                    _listActiveDownloads.addView(view)
            }

        }
    }
}
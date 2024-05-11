package com.futo.platformplayer.views.adapters

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.futo.platformplayer.R
import com.futo.platformplayer.api.media.structures.TemporalItem
import com.futo.platformplayer.fragment.mainactivity.main.DownloadsFragment

abstract class DownloadFilterableAdapter<Data: Any, Holder: AnyAdapter.AnyViewHolder<Data>>(
    protected val _frag: DownloadsFragment,
    protected val _view: View
) : RecyclerView.Adapter<Holder>() {
    private val _containerSortBy: LinearLayout = _view.findViewById(R.id.sortby_container)
    private val _containerSearch: FrameLayout = _view.findViewById(R.id.container_search)
    private val _editSearch: EditText = _view.findViewById(R.id.edit_search)
    private val _clearSearch: ImageButton = _view.findViewById(R.id.button_clear_search)

    protected lateinit var _filteredDataset: List<Data>
    protected lateinit var _originalDataset: List<TemporalItem<Data>>

    var sortBy = 0
        set(value) {
            field = value
            updateDataset()
        }
    var query: String? = null
        set(value) {
            field = value
            updateDataset()
            updateContainer()
        }

    init {
        val spinnerSortBy: Spinner = _view.findViewById(R.id.spinner_sortby)
        spinnerSortBy.adapter = ArrayAdapter(_view.context, R.layout.spinner_item_simple, _view.resources.getStringArray(
            R.array.downloads_sortby_array)).also {
            it.setDropDownViewResource(R.layout.spinner_dropdownitem_simple)
        }
        spinnerSortBy.setSelection(sortBy)
        spinnerSortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sortBy = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        _editSearch.addTextChangedListener {
            query = it.toString()
        }

        _clearSearch.setOnClickListener {
            query = null
        }
    }
    override fun getItemCount() = _filteredDataset.size

    open val sourceCount get() = _originalDataset.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(_filteredDataset[position])
    }

    protected abstract fun updateContainer()

    protected abstract fun updateDataset()
}
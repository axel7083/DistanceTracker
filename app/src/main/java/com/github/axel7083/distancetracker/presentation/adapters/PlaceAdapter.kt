package com.github.axel7083.distancetracker.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.axel7083.distancetracker.R
import com.github.axel7083.distancetracker.core.api.data.Place
import com.github.axel7083.distancetracker.databinding.RowPlaceDetailsBinding

class PlaceAdapter(
    private var data: List<Place>
):
    RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: RowPlaceDetailsBinding
        init {
            binding = RowPlaceDetailsBinding.bind(view)
        }
    }

    fun updateDataSet(data: List<Place>) {
        this.data = data
        notifyItemRangeInserted(0, this.data.size)
    }

    fun clear() {
        val oldSize = this.data.size
        this.data = emptyList()
        notifyItemRangeRemoved(0, oldSize)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_place_details, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = data[position].display_name
    }

    override fun getItemCount(): Int = data.size
}
package com.github.axel7083.distancetracker.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.axel7083.distancetracker.R
import com.github.axel7083.distancetracker.core.api.data.Place
import com.github.axel7083.distancetracker.core.room.entities.PlaceEntity
import com.github.axel7083.distancetracker.databinding.RowPlaceDetailsBinding

class PlaceAdapter(
    private var data: List<PlaceEntity>,
    private val onItemClicked: (PlaceEntity) -> Unit
):
    RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: RowPlaceDetailsBinding
        init {
            binding = RowPlaceDetailsBinding.bind(view)
            binding.explore.setOnClickListener {
                onItemClicked.invoke(data[adapterPosition])
            }
        }
    }

    fun updateDataSet(data: List<PlaceEntity>) {
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
        val subs = data[position].name.split(",")
        when(subs.size) {
            0 -> {
                holder.binding.name.text = ""
                holder.binding.details.text = ""
            }
            1 -> {
                holder.binding.name.text = subs[0]
                holder.binding.details.text = ""
            }
            else -> {
                holder.binding.name.text = subs[0]
                holder.binding.details.text = subs.minus(subs.first()).joinToString()
            }
        }

    }

    override fun getItemCount(): Int = data.size
}
package me.jfenn.alarmio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView

import me.jfenn.alarmio.data.preference.BasePreferenceData
import me.jfenn.alarmio.data.preference.BasePreferenceData.ViewHolder


class PreferenceAdapter(private val items: ArrayList<BasePreferenceData<ViewHolder>>) :
    RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return items[viewType].getViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].bindViewHolder(holder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}

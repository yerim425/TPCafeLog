package com.yrlee.tpcafelog.ui.review

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yrlee.tpcafelog.R
import com.yrlee.tpcafelog.databinding.ItemCafeNameBinding
import com.yrlee.tpcafelog.model.CafeItem
import com.yrlee.tpcafelog.model.VisitCafeInfoItem

class VisitedCafeAdapter(
    private val context: Context,
    private val items: List<VisitCafeInfoItem>,
    private val onItemClick: (VisitCafeInfoItem) -> Unit
) : Adapter<VisitedCafeAdapter.VH>() {

    inner class VH(val binding: ItemCafeNameBinding) : ViewHolder(binding.root) {

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCafeNameBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvCafeName.text = item.place_name
        holder.binding.tvCafeAddress.text = item.place_address
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
package com.yrlee.tpcafelog.ui.review

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yrlee.tpcafelog.databinding.ItemCafeNameBinding
import com.yrlee.tpcafelog.model.VisitedCafeItem

class VisitedCafeAdapter(
    private val context: Context,
    private val itemList: List<VisitedCafeItem>,
    private val onItemClick: (VisitedCafeItem) -> Unit
) : Adapter<VisitedCafeAdapter.VH>() {

    inner class VH(val binding: ItemCafeNameBinding) : ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick(itemList[bindingAdapterPosition])
            }
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCafeNameBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = itemList[position]
        holder.binding.tvCafeName.text = item.place_name
        holder.binding.tvCafeAddress.text = item.place_address
        Log.d("visited cafe adapter", "${item.place_name}/${item.place_address}")
    }

    override fun getItemCount() = itemList.size
}